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

import com.forcetower.sagres.database.model.SagresDisciplineGroup
import com.forcetower.sagres.decoders.Base64Encoder
import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresDisciplineDetailsFetcherParser
import com.forcetower.sagres.parsers.SagresDisciplineDetailsParser
import com.forcetower.sagres.parsers.SagresMaterialsParser
import com.forcetower.sagres.request.SagresCalls
import okhttp3.FormBody
import okhttp3.RequestBody
import org.json.JSONObject
import org.jsoup.nodes.Document
import java.io.IOException

class FastDisciplinesOperation(
    private val semester: String?,
    private val code: String?,
    private val group: String?,
    private val partialLoad: Boolean,
    private val discover: Boolean,
    private val encoder: Base64Encoder,
    private val caller: SagresCalls
) : Operation<FastDisciplinesCallback> {

    override suspend fun execute(): FastDisciplinesCallback {
        val (initial, iniCallback) = initial()
        if (initial == null) return iniCallback!!

        val (disciplines, disCallback) = disciplines(initial)
        if (disciplines == null) return disCallback!!

        val semesters = processSemesters(disciplines)
        val discovered = if (discover && semester == null) semesters.maxByOrNull { it.first }?.second else semester
        val bodies = processDisciplines(disciplines, discovered) ?: return FastDisciplinesCallback(Status.UNKNOWN_FAILURE)
        return executeCalls(bodies, semesters)
    }

    private fun processSemesters(document: Document): List<Pair<Long, String>> {
        return SagresDisciplineDetailsFetcherParser.extractSemesters(document)
    }

    private suspend fun executeCalls(bodies: List<RequestBody?>, semesters: List<Pair<Long, String>>): FastDisciplinesCallback {
        val groups = mutableListOf<SagresDisciplineGroup>()
        var failureCount = 0
//        val total = bodies.filterNotNull().size

        for (body in bodies.filterNotNull()) {
//            publishProgress(FastDisciplinesCallback(Status.LOADING).flags(DOWNLOADING).current(index).total(total))
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
        return FastDisciplinesCallback(Status.COMPLETED)
            .groups(groups)
            .failureCount(failureCount)
            .semesters(semesters)
    }

    private suspend fun initialFormConnect(body: RequestBody): Document? {
        val call = caller.goToDisciplineAlternate(body)
        try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val value = response.body!!.string()
                return value.asDocument()
            }
        } catch (_: IOException) {}
        return null
    }

    private suspend fun disciplinePageParams(params: FormBody.Builder): Document? {
        val call = caller.getDisciplinePageWithParams(params)
        try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return body.asDocument()
            }
        } catch (_: IOException) { }
        return null
    }

    private fun processGroup(document: Document): SagresDisciplineGroup? {
        return SagresDisciplineDetailsParser.extractDisciplineGroup(document)
    }

    private suspend fun downloadMaterials(document: Document, group: SagresDisciplineGroup) {
        val items = group.classItems.filter { it.numberOfMaterials > 0 }
        for (item in items) {
            if (item.numberOfMaterials <= 0) continue
            val json = JSONObject()
            json.put("_realType", true)
            json.put("showForm", true)
            json.put("popupLinkColumn", "cpt_material_apoio")
            json.put("retrieveArguments", item.materialLink)
            val encoded = encoder.encodeString(json.toString())
            val materials = executeMaterialCall(document, encoded)
            if (materials != null) {
                item.materials = SagresMaterialsParser.extractMaterials(materials)
            }
        }
    }

    private suspend fun executeMaterialCall(document: Document, encoded: String): Document? {
        val call = caller.getDisciplineMaterials(encoded, document)
        try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return body.asDocument()
            }
        } catch (_: IOException) { }
        return null
    }

    private fun processDisciplines(document: Document, semester: String?): List<RequestBody?>? {
        return SagresDisciplineDetailsFetcherParser.extractFastForms(document, semester, code, group)
    }

    private suspend fun disciplines(document: Document): Pair<Document?, FastDisciplinesCallback?> {
        val call = caller.postAllDisciplinesParams(document)
        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val value = response.body!!.string()
                value.asDocument() to null
            } else {
                null to FastDisciplinesCallback(Status.RESPONSE_FAILED).code(response.code)
            }
        } catch (t: Throwable) {
            null to FastDisciplinesCallback(Status.NETWORK_ERROR).throwable(t)
        }
    }

    private suspend fun initial(): Pair<Document?, FastDisciplinesCallback?> {
        val call = caller.getAllDisciplinesPage()
        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val value = response.body!!.string()
                return value.asDocument() to null
            } else {
                null to FastDisciplinesCallback(Status.RESPONSE_FAILED).code(response.code)
            }
        } catch (t: Throwable) {
            null to FastDisciplinesCallback(Status.NETWORK_ERROR).throwable(t)
        }
    }
}
