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

package com.forcetower.sagres.request

import com.forcetower.sagres.database.model.SagresDemandOffer
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.jsoup.nodes.Document

class SagresCalls(
    private val client: OkHttpClient,
    val selectedInstitution: () -> String
) {
    val startPage: Call
        get() {
            val request = SagresRequests.startPage(selectedInstitution())
            return getCall(request)
        }

    private fun getCall(request: Request): Call {
        return client.newCall(request)
    }

    fun login(username: String, password: String, gresp: String? = null): Call {
        val body = SagresForms.loginBody(username, password, gresp, selectedInstitution())
        val request = SagresRequests.loginRequest(body, selectedInstitution())
        return getCall(request)
    }

    fun loginApproval(document: Document, response: Response): Call {
        val responsePath = response.request.url.toUrl()
        val url = responsePath.host + responsePath.path
        val body = SagresForms.loginApprovalBody(document)
        val request = SagresRequests.loginApprovalRequest(url, body)
        return getCall(request)
    }

    fun getGrades(semester: Long?, document: Document?, variant: Long? = null): Call {
        val request = if (semester == null) {
            SagresRequests.currentGrades(selectedInstitution())
        } else {
            SagresRequests.getGradesForSemester(semester, document!!, variant, selectedInstitution())
        }

        return getCall(request)
    }

    fun getPageCall(url: String): Call {
        return getCall(SagresRequests.getPageRequest(url))
    }

    fun getDisciplinePageFromInitial(form: FormBody.Builder): Call {
        return getCall(SagresRequests.postAtStudentPage(form, selectedInstitution()))
    }

    fun getDisciplinePageWithParams(params: FormBody.Builder): Call {
        return getCall(SagresRequests.getDisciplinePageWithParams(params, selectedInstitution()))
    }

    fun getDisciplineMaterials(encoded: String, document: Document): Call {
        val body = SagresForms.makeFormBodyForDisciplineMaterials(document, encoded)
        val request = SagresRequests.getDisciplinePageWithParams(body, selectedInstitution())
        return getCall(request)
    }

    fun getDemandPage(): Call {
        return getCall(SagresRequests.demandPage(selectedInstitution()))
    }

    fun createDemand(list: List<SagresDemandOffer>, document: Document): Call {
        val body = SagresForms.makeFormBodyForDemand(list, document)
        val request = SagresRequests.createDemandWithParams(body, selectedInstitution())
        return getCall(request)
    }

    fun getRequestedServices(): Call {
        return getCall(SagresRequests.requestedServices(selectedInstitution()))
    }

    fun getMessagesPage(): Call {
        return getCall(SagresRequests.messagesPage(selectedInstitution()))
    }

    fun getAllDisciplinesPage(): Call {
        return getCall(SagresRequests.allDisciplinesPage(selectedInstitution()))
    }

    fun postAllDisciplinesParams(document: Document): Call {
        val body = SagresForms.makeFormBodyForAllDisciplines(document)
        val request = SagresRequests.postAllDisciplinesParams(body, selectedInstitution())
        return getCall(request)
    }

    fun goToDisciplineAlternate(body: RequestBody): Call {
        val request = SagresRequests.postAllDisciplinesParams(body, selectedInstitution())
        return getCall(request)
    }

    fun onMyZsH(): Call {
        val request = SagresRequests.ohMyZsh(selectedInstitution())
        return getCall(request)
    }
}
