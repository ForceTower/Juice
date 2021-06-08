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

package com.forcetower.sagres.operation.disciplinedetails

import com.forcetower.sagres.database.model.SagresDisciplineGroup
import com.forcetower.sagres.decoders.Base64Encoder
import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.operation.initial.StartPageCallback
import com.forcetower.sagres.operation.initial.StartPageOperation
import com.forcetower.sagres.parsers.SagresDisciplineDetailsFetcherParser
import com.forcetower.sagres.parsers.SagresDisciplineDetailsParser
import com.forcetower.sagres.parsers.SagresMaterialsParser
import com.forcetower.sagres.request.SagresCalls
import okhttp3.FormBody
import org.json.JSONObject
import org.jsoup.nodes.Document
import java.io.IOException

class DisciplineDetailsOperation(
    private val semester: String?,
    private val code: String?,
    private val group: String?,
    private val partialLoad: Boolean,
    private val encoder: Base64Encoder,
    private val caller: SagresCalls
) : Operation<DisciplineDetailsCallback> {
    override suspend fun execute(): DisciplineDetailsCallback {
        val initial = initialPage() ?: return DisciplineDetailsCallback(Status.NETWORK_ERROR)

        val forms = SagresDisciplineDetailsFetcherParser.extractFormBodies(initial.document!!, semester, code, group)
        val groups = mutableListOf<SagresDisciplineGroup>()

        var failureCount = 0
        for (form in forms) {
            val document = initialFormConnect(form.first)
            if (document != null) {
                val params = SagresDisciplineDetailsFetcherParser.extractParamsForDiscipline(document)
                val discipline = if (partialLoad) document else disciplinePageParams(params)
                if (discipline != null) {
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
            } else {
                failureCount++
            }
        }
        return DisciplineDetailsCallback(Status.COMPLETED).groups(groups).failureCount(failureCount)
    }

    private suspend fun initialPage(): StartPageCallback? {
        val initial = StartPageOperation(caller).execute()
        if (initial.status != Status.SUCCESS) {
            return null
        }
        return initial
    }

    private suspend fun initialFormConnect(form: FormBody.Builder): Document? {
        val call = caller.getDisciplinePageFromInitial(form)
        try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return body.asDocument()
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
        } catch (_: IOException) {}
        return null
    }
}
