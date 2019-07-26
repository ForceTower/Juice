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
import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.impl.SagresNavigatorImpl
import com.forcetower.sagres.operation.BaseCallback
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.operation.disciplinedetails.DisciplineDetailsCallback.Companion.DOWNLOADING
import com.forcetower.sagres.operation.disciplinedetails.DisciplineDetailsCallback.Companion.INITIAL
import com.forcetower.sagres.operation.disciplinedetails.DisciplineDetailsCallback.Companion.PROCESSING
import com.forcetower.sagres.operation.initial.StartPageOperation
import com.forcetower.sagres.parsers.SagresDisciplineDetailsFetcherParser
import com.forcetower.sagres.parsers.SagresDisciplineDetailsParser
import com.forcetower.sagres.parsers.SagresMaterialsParser
import com.forcetower.sagres.request.SagresCalls
import okhttp3.FormBody
import org.apache.commons.codec.binary.Base64
import org.json.JSONObject
import org.jsoup.nodes.Document
import timber.log.Timber
import timber.log.debug
import java.io.IOException
import java.util.concurrent.Executor

class DisciplineDetailsOperation(
    private val semester: String?,
    private val code: String?,
    private val group: String?,
    private val partialLoad: Boolean,
    executor: Executor?
) : Operation<DisciplineDetailsCallback>(executor) {

    init {
        this.perform()
    }

    override fun execute() {
        executeSteps()
    }

    private fun executeSteps() {
        publishProgress(DisciplineDetailsCallback(Status.LOADING).flags(INITIAL))
        val initial = initialPage() ?: return
        publishProgress(DisciplineDetailsCallback(Status.LOADING).flags(PROCESSING))
        val forms = SagresDisciplineDetailsFetcherParser.extractFormBodies(initial.document!!, semester, code, group)
        val groups = mutableListOf<SagresDisciplineGroup>()

        var failureCount = 0

        val total = forms.size
        for ((index, form) in forms.withIndex()) {
            publishProgress(DisciplineDetailsCallback(Status.LOADING).flags(DOWNLOADING).current(index).total(total))
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
                        Timber.debug { "Processed group was null" }
                    }
                } else {
                    failureCount++
                    Timber.debug { "Discipline from params was null" }
                }
            } else {
                failureCount++
                Timber.debug { "Document from initial was null" }
            }
        }
        Timber.debug { "Completed ${forms.size} -- $semester $code $group" }
        publishProgress(DisciplineDetailsCallback(Status.COMPLETED).groups(groups).failureCount(failureCount))
    }

    private fun initialPage(): BaseCallback<*>? {
        Timber.debug { "Initial" }
        val initial = StartPageOperation(null).finishedResult
        if (initial.status != Status.SUCCESS) {
            publishProgress(DisciplineDetailsCallback.copyFrom(initial))
            return null
        }
        return initial
    }

    private fun initialFormConnect(form: FormBody.Builder): Document? {
        Timber.debug { "Going to Discipline Page" }
        val call = SagresCalls.getDisciplinePageFromInitial(form)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return body.asDocument()
            } else {
                publishProgress(DisciplineDetailsCallback(Status.RESPONSE_FAILED).message("Unsuccessful response").code(response.code))
            }
        } catch (e: IOException) {
            publishProgress(DisciplineDetailsCallback(Status.NETWORK_ERROR).throwable(e).message("Failed at initial form connect"))
        }
        return null
    }

    private fun disciplinePageParams(params: FormBody.Builder): Document? {
        Timber.debug { "Discipline with Params" }
        val call = SagresCalls.getDisciplinePageWithParams(params)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return body.asDocument()
            } else {
                publishProgress(DisciplineDetailsCallback(Status.RESPONSE_FAILED).message("Unsuccessful response at params").code(response.code))
            }
        } catch (e: IOException) {
            publishProgress(DisciplineDetailsCallback(Status.NETWORK_ERROR).throwable(e).message("Failed at params setup"))
        }
        return null
    }

    private fun processGroup(document: Document): SagresDisciplineGroup? {
        Timber.debug { "Processing group" }
        return SagresDisciplineDetailsParser.extractDisciplineGroup(document)
    }

    private fun downloadMaterials(document: Document, group: SagresDisciplineGroup) {
        Timber.debug { "Initializing materials download" }
        val items = group.classItems?.filter { it.numberOfMaterials > 0 } ?: return
        for (item in items) {
            val json = JSONObject()
            json.put("_realType", true)
            json.put("showForm", true)
            json.put("popupLinkColumn", "cpt_material_apoio")
            json.put("retrieveArguments", item.materialLink)
            val encoded = Base64.encodeBase64String(json.toString().toByteArray())
            val materials = executeMaterialCall(document, encoded)
            if (materials != null) {
                item.materials = SagresMaterialsParser.extractMaterials(materials)
            }
        }
    }

    private fun executeMaterialCall(document: Document, encoded: String): Document? {
        Timber.debug { "Executing materials call" }
        val call = SagresCalls.getDisciplineMaterials(encoded, document)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return body.asDocument()
            } else {
                publishProgress(DisciplineDetailsCallback(Status.RESPONSE_FAILED).message("Unsuccessful response at material download"))
            }
        } catch (e: IOException) {
            publishProgress(DisciplineDetailsCallback(Status.NETWORK_ERROR).throwable(e).message("Failed to fetch material"))
        }
        return null
    }
}