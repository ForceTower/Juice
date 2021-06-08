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

package com.forcetower.sagres.operation.disciplines

import com.forcetower.sagres.SagresNavigator
import com.forcetower.sagres.database.model.SagresDisciplineGroup
import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.operation.disciplines.FastDisciplinesCallback.Companion.DOWNLOADING
import com.forcetower.sagres.operation.disciplines.FastDisciplinesCallback.Companion.INITIAL
import com.forcetower.sagres.operation.disciplines.FastDisciplinesCallback.Companion.PROCESSING
import com.forcetower.sagres.parsers.SagresDisciplineDetailsFetcherParser
import com.forcetower.sagres.parsers.SagresDisciplineDetailsParser
import com.forcetower.sagres.parsers.SagresMaterialsParser
import com.forcetower.sagres.request.SagresCalls
import java.io.IOException
import java.util.concurrent.Executor
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONObject
import org.jsoup.nodes.Document

class FastDisciplinesOperation(
    private val semester: String?,
    private val code: String?,
    private val group: String?,
    private val partialLoad: Boolean,
    private val discover: Boolean,
    executor: Executor?
) : Operation<FastDisciplinesCallback>(executor) {

    init {
        this.perform()
    }

    override fun execute() {
        executeSteps()
    }

    private fun executeSteps() {
        publishProgress(FastDisciplinesCallback(Status.LOADING).flags(INITIAL))
        val initial = initial() ?: return
        publishProgress(FastDisciplinesCallback(Status.LOADING).flags(INITIAL))
        val disciplines = disciplines(initial) ?: return
        publishProgress(FastDisciplinesCallback(Status.LOADING).flags(PROCESSING))
        val semesters = processSemesters(disciplines)
        val discovered = if (discover && semester == null) semesters.maxByOrNull { it.first }?.second else semester
        val bodies = processDisciplines(disciplines, discovered) ?: return
        executeCalls(bodies, semesters)
    }

    private fun processSemesters(document: Document): List<Pair<Long, String>> {
        return SagresDisciplineDetailsFetcherParser.extractSemesters(document)
    }

    private fun executeCalls(bodies: List<RequestBody?>, semesters: List<Pair<Long, String>>) {
        val groups = mutableListOf<SagresDisciplineGroup>()
        var failureCount = 0
        val total = bodies.filterNotNull().size

        for ((index, body) in bodies.filterNotNull().withIndex()) {
            publishProgress(FastDisciplinesCallback(Status.LOADING).flags(DOWNLOADING).current(index).total(total))
            val document = initialFormConnect(body)
            if (document != null) {
                val params = SagresDisciplineDetailsFetcherParser.extractParamsForDiscipline(document, true)
                val discipline = if (partialLoad) document else disciplinePageParams(params) ?: document
                val group = processGroup(discipline)
                if (group != null) {
                    if (!partialLoad) downloadMaterials(discipline, group)
                    groups.add(group)
                } else {
                    failureCount++
                }
            } else {
                failureCount++
            }
        }
        publishProgress(
                FastDisciplinesCallback(Status.COMPLETED)
                        .groups(groups)
                        .failureCount(failureCount)
                        .semesters(semesters)
        )
    }

    private fun initialFormConnect(body: RequestBody?): Document? {
        body ?: return null
        val call = SagresCalls.goToDisciplineAlternate(body)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val value = response.body!!.string()
                val document = value.asDocument()
                return document
            } else {
                publishProgress(FastDisciplinesCallback(Status.RESPONSE_FAILED).message("Unsuccessful response").code(response.code))
            }
        } catch (e: IOException) {
            publishProgress(FastDisciplinesCallback(Status.NETWORK_ERROR).throwable(e).message("Failed at initial form connect"))
        }
        return null
    }

    private fun disciplinePageParams(params: FormBody.Builder): Document? {
        val call = SagresCalls.getDisciplinePageWithParams(params)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return body.asDocument()
            } else {
                publishProgress(FastDisciplinesCallback(Status.RESPONSE_FAILED).message("Unsuccessful response at params").code(response.code))
            }
        } catch (e: IOException) {
            publishProgress(FastDisciplinesCallback(Status.NETWORK_ERROR).throwable(e).message("Failed at params setup"))
        }
        return null
    }

    private fun processGroup(document: Document): SagresDisciplineGroup? {
        return SagresDisciplineDetailsParser.extractDisciplineGroup(document)
    }

    private fun downloadMaterials(document: Document, group: SagresDisciplineGroup) {
        val items = group.classItems.filter { it.numberOfMaterials > 0 }
        for (item in items) {
            if (item.numberOfMaterials <= 0) continue
            val json = JSONObject()
            json.put("_realType", true)
            json.put("showForm", true)
            json.put("popupLinkColumn", "cpt_material_apoio")
            json.put("retrieveArguments", item.materialLink)
            val encoder = SagresNavigator.instance.getBase64Encoder()
            val encoded = encoder.encodeString(json.toString())
            val materials = executeMaterialCall(document, encoded)
            if (materials != null) {
                item.materials = SagresMaterialsParser.extractMaterials(materials)
            }
        }
    }

    private fun executeMaterialCall(document: Document, encoded: String): Document? {
        val call = SagresCalls.getDisciplineMaterials(encoded, document)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return body.asDocument()
            } else {
                publishProgress(FastDisciplinesCallback(Status.RESPONSE_FAILED).message("Unsuccessful response at material download"))
            }
        } catch (e: IOException) {
            publishProgress(FastDisciplinesCallback(Status.NETWORK_ERROR).throwable(e).message("Failed to fetch material"))
        }
        return null
    }

    private fun processDisciplines(document: Document, semester: String?): List<RequestBody?>? {
        return SagresDisciplineDetailsFetcherParser.extractFastForms(document, semester, code, group)
    }

    private fun disciplines(document: Document): Document? {
        val call = SagresCalls.postAllDisciplinesParams(document)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val value = response.body!!.string()
                return value.asDocument()
            } else {
                publishProgress(FastDisciplinesCallback(Status.RESPONSE_FAILED).code(response.code))
            }
        } catch (t: Throwable) {
            publishProgress(FastDisciplinesCallback(Status.NETWORK_ERROR).throwable(t))
        }
        return null
    }

    private fun initial(): Document? {
        val call = SagresCalls.getAllDisciplinesPage()
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val value = response.body!!.string()
                return value.asDocument()
            } else {
                publishProgress(FastDisciplinesCallback(Status.RESPONSE_FAILED).code(response.code))
            }
        } catch (t: Throwable) {
            publishProgress(FastDisciplinesCallback(Status.NETWORK_ERROR).throwable(t))
        }
        return null
    }
}
