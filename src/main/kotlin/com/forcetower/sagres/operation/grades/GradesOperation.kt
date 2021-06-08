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
import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresGradesParser
import com.forcetower.sagres.parsers.SagresMissedClassesParser
import com.forcetower.sagres.request.SagresCalls
import org.jsoup.nodes.Document
import java.io.IOException
import java.lang.Exception

class GradesOperation(
    private val semester: Long?,
    private val document: Document?,
    private val caller: SagresCalls
) : Operation<GradesCallback> {

    override suspend fun execute(): GradesCallback {
        val call = caller.getGrades(semester, document)
        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                processResults(body)
            } else {
                GradesCallback(Status.RESPONSE_FAILED).code(response.code).message(response.message)
            }
        } catch (e: IOException) {
            GradesCallback(Status.NETWORK_ERROR).throwable(e)
        }
    }

    private suspend fun processResults(body: String): GradesCallback {
        val document = body.asDocument()
        val codes = SagresGradesParser.extractSemesterCodes(document)
        val selected = SagresGradesParser.getSelectedSemester(document)

        return if (selected != null) {
            if (SagresGradesParser.canExtractGrades(document)) {
                val grades = SagresGradesParser.extractGrades(document, selected.second)
                val frequency = SagresMissedClassesParser.extractMissedClasses(document, selected.second)
                successMeasures(document, codes, grades, frequency)
            } else {
                val variants = SagresGradesParser.extractCourseVariants(document)
                if (variants.isEmpty()) {
                    GradesCallback(Status.APPROVAL_ERROR).message("Can't extract grades and there's no variant. Page error?")
                } else {
                    variantRequester(variants, document, selected.second, codes)
                }
            }
        } else {
            GradesCallback(Status.APPROVAL_ERROR).message("Can't find semester on situation. Nothing is selected")
        }
    }

    // This scaled really quickly
    private suspend fun variantRequester(
        variants: List<SagresCourseVariant>,
        document: Document,
        semester: Long,
        codes: List<Pair<Long, String>>
    ): GradesCallback {
        val grades = mutableListOf<SagresGrade>()
        val frequency = mutableListOf<SagresDisciplineMissedClass>()
        var doc = document
        variants.forEach {
            val result = requestVariant(semester, doc, it.uefsId)
            grades += result.grades
            frequency += result.frequency
            doc = result.document
        }
        return successMeasures(document, codes, grades, true to frequency)
    }

    private suspend fun requestVariant(semester: Long, document: Document, variant: Long? = null): GradeResult {
        val call = caller.getGrades(semester, document, variant)
        return try {
            val response = call.executeSuspend()
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
    ): GradesCallback {
        return GradesCallback(Status.SUCCESS)
            .document(document)
            .grades(grades)
            .frequency(if (frequency.first) null else frequency.second)
            .codes(codes)
    }

    private data class GradeResult(
        val grades: List<SagresGrade> = emptyList(),
        val frequency: List<SagresDisciplineMissedClass> = emptyList(),
        val document: Document
    )
}
