/*
 * This file is part of the UNES Open Source Project.
 * UNES is licensed under the GNU GPLv3.
 *
 * Copyright (c) 2019. João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.forcetower.sagres.operation.grades

import com.forcetower.sagres.database.model.SagresCourseVariant
import com.forcetower.sagres.database.model.SagresDisciplineMissedClass
import com.forcetower.sagres.database.model.SagresGrade
import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresGradesParser
import com.forcetower.sagres.parsers.SagresMissedClassesParser
import com.forcetower.sagres.request.SagresCalls
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.Executor
import org.jsoup.nodes.Document

class GradesOperation(private val semester: Long?, private val document: Document?, executor: Executor?) : Operation<GradesCallback>(executor) {
    init {
        this.perform()
    }

    override fun execute() {
        val call = SagresCalls.getGrades(semester, document)
        try {
            publishProgress(GradesCallback(Status.LOADING))
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                processResults(body)
            } else {
                publishProgress(GradesCallback(Status.RESPONSE_FAILED).code(response.code).message(response.message))
            }
        } catch (e: IOException) {
            publishProgress(GradesCallback(Status.NETWORK_ERROR).throwable(e))
        }
    }

    private fun processResults(body: String) {
        val document = body.asDocument()
        val codes = SagresGradesParser.extractSemesterCodes(document)
        val selected = SagresGradesParser.getSelectedSemester(document)

        if (selected != null) {
            if (SagresGradesParser.canExtractGrades(document)) {
                val grades = SagresGradesParser.extractGrades(document, selected.second)
                val frequency = SagresMissedClassesParser.extractMissedClasses(document, selected.second)
                successMeasures(document, codes, grades, frequency)
            } else {
                val variants = SagresGradesParser.extractCourseVariants(document)
                if (variants.isEmpty()) {
                    publishProgress(GradesCallback(Status.APPROVAL_ERROR).message("Can't extract grades and there's no variant. Page error?"))
                } else {
                    variantRequester(variants, document, selected.second, codes)
                }
            }
        } else {
            publishProgress(GradesCallback(Status.APPROVAL_ERROR).message("Can't find semester on situation. Nothing is selected"))
        }
    }

    private fun variantRequester(
        variants: List<SagresCourseVariant>,
        document: Document,
        semester: Long,
        codes: List<Pair<Long, String>>
    ) {
        val grades = mutableListOf<SagresGrade>()
        val frequency = mutableListOf<SagresDisciplineMissedClass>()
        var doc = document
        variants.forEach {
            val result = requestVariant(semester, doc, it.uefsId)
            grades += result.grades
            frequency += result.frequency
            doc = result.document
        }
        successMeasures(document, codes, grades, true to frequency)
    }

    private fun requestVariant(semester: Long, document: Document, variant: Long? = null): GradeResult {
        val call = SagresCalls.getGrades(semester, document, variant)
        return try {
            val response = call.execute()
            val body = response.body!!.string()
            processVariant(body, semester)
        } catch (e: Exception) {
            GradeResult(document = document)
        }
    }

    private fun processVariant(body: String, semester: Long): GradeResult {
        val document = body.asDocument()
        return if (SagresGradesParser.canExtractGrades(document)) {
            val grades = SagresGradesParser.extractGrades(document, semester)
            val frequency = SagresMissedClassesParser.extractMissedClasses(document, semester)
            GradeResult(grades, frequency.second, document)
        } else {
            GradeResult(document = document)
        }
    }

    private fun successMeasures(
        document: Document,
        codes: List<Pair<Long, String>>,
        grades: List<SagresGrade>,
        frequency: Pair<Boolean, List<SagresDisciplineMissedClass>>
    ) {
        publishProgress(GradesCallback(Status.SUCCESS)
                .document(document)
                .grades(grades)
                .frequency(if (frequency.first) null else frequency.second)
                .codes(codes))
    }

    private data class GradeResult(
        val grades: List<SagresGrade> = emptyList(),
        val frequency: List<SagresDisciplineMissedClass> = emptyList(),
        val document: Document
    )
}
