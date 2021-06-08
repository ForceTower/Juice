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

package com.forcetower.sagres.impl

import com.forcetower.sagres.SagresNavigator
import com.forcetower.sagres.cookies.CookiePersistor
import com.forcetower.sagres.cookies.PersistentCookieJar
import com.forcetower.sagres.cookies.SetCookieCache
import com.forcetower.sagres.database.model.SagresDemandOffer
import com.forcetower.sagres.decoders.Base64Encoder
import com.forcetower.sagres.operation.calendar.CalendarCallback
import com.forcetower.sagres.operation.calendar.CalendarOperation
import com.forcetower.sagres.operation.demand.CreateDemandOperation
import com.forcetower.sagres.operation.demand.DemandCreatorCallback
import com.forcetower.sagres.operation.demand.DemandOffersCallback
import com.forcetower.sagres.operation.demand.LoadDemandOffersOperation
import com.forcetower.sagres.operation.disciplinedetails.DisciplineDetailsCallback
import com.forcetower.sagres.operation.disciplinedetails.DisciplineDetailsOperation
import com.forcetower.sagres.operation.disciplines.FastDisciplinesCallback
import com.forcetower.sagres.operation.disciplines.FastDisciplinesOperation
import com.forcetower.sagres.operation.document.DocumentCallback
import com.forcetower.sagres.operation.document.DocumentOperation
import com.forcetower.sagres.operation.grades.GradesCallback
import com.forcetower.sagres.operation.grades.GradesOperation
import com.forcetower.sagres.operation.initial.StartPageCallback
import com.forcetower.sagres.operation.initial.StartPageOperation
import com.forcetower.sagres.operation.login.LoginCallback
import com.forcetower.sagres.operation.login.LoginOperation
import com.forcetower.sagres.operation.messages.MessagesCallback
import com.forcetower.sagres.operation.messages.OldMessagesOperation
import com.forcetower.sagres.operation.ohmyzsh.DoneCallback
import com.forcetower.sagres.operation.ohmyzsh.JustDoIt
import com.forcetower.sagres.operation.servicerequest.RequestedServicesCallback
import com.forcetower.sagres.operation.servicerequest.RequestedServicesOperation
import com.forcetower.sagres.request.SagresCalls
import okhttp3.Call
import okhttp3.ConnectionSpec
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import java.io.File
import java.util.concurrent.TimeUnit

class SagresNavigatorImpl private constructor(
    persist: CookiePersistor?,
    private val base64Encoder: Base64Encoder,
    baseClient: OkHttpClient?
) : SagresNavigator() {
    private val cookies = SetCookieCache()
    private val cookieJar = createCookieJar(cookies, persist)
    private val client: OkHttpClient = createClient(cookieJar, baseClient)
    private var selectedInstitution = "UEFS"
    private val caller = SagresCalls(client) { selectedInstitution }

    private fun createClient(cookies: CookieJar, baseClient: OkHttpClient?): OkHttpClient {
        return (baseClient?.newBuilder() ?: OkHttpClient.Builder())
            .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
            .followRedirects(true)
            .cookieJar(cookies)
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    private fun createCookieJar(cookies: SetCookieCache, persist: CookiePersistor?): PersistentCookieJar {
        return PersistentCookieJar(cookies, persist)
    }

    override suspend fun login(username: String, password: String, gresp: String?): LoginCallback {
        return LoginOperation(username, password, gresp, caller).execute()
    }

    override suspend fun messagesHtml(): MessagesCallback {
        return OldMessagesOperation(caller).execute()
    }

    override suspend fun calendar(): CalendarCallback {
        return CalendarOperation(caller).execute()
    }

    override suspend fun startPage(): StartPageCallback {
        return StartPageOperation(caller).execute()
    }

    override suspend fun getCurrentGrades(): GradesCallback {
        return GradesOperation(null, null, caller).execute()
    }

    override suspend fun getGradesFromSemester(semesterSagresId: Long, document: Document): GradesCallback {
        return GradesOperation(semesterSagresId, document, caller).execute()
    }

    override suspend fun downloadEnrollment(file: File): DocumentCallback {
        return DocumentOperation(file, "SAGRES_ENROLL_CERT", caller).execute()
    }

    override suspend fun downloadFlowchart(file: File): DocumentCallback {
        return DocumentOperation(file, "SAGRES_FLOWCHART", caller).execute()
    }

    override suspend fun downloadHistory(file: File): DocumentCallback {
        return DocumentOperation(file, "SAGRES_HISTORY", caller).execute()
    }

    override suspend fun loadDisciplineDetails(semester: String?, code: String?, group: String?, partialLoad: Boolean): DisciplineDetailsCallback {
        return DisciplineDetailsOperation(semester, code, group, partialLoad, base64Encoder, caller).execute()
    }

    override suspend fun loadDemandOffers(): DemandOffersCallback {
        return LoadDemandOffersOperation(caller).execute()
    }

    override suspend fun createDemandOffer(offers: List<SagresDemandOffer>): DemandCreatorCallback {
        return CreateDemandOperation(offers, caller).execute()
    }

    override suspend fun getRequestedServices(): RequestedServicesCallback {
        return RequestedServicesOperation(caller).execute()
    }

    override suspend fun disciplinesExperimental(semester: String?, code: String?, group: String?, partialLoad: Boolean, discover: Boolean): FastDisciplinesCallback {
        return FastDisciplinesOperation(semester, code, group, partialLoad, discover, base64Encoder, caller).execute()
    }

    override suspend fun ohMyZsh(): DoneCallback {
        return JustDoIt(caller).execute()
    }

    override fun getSelectedInstitution() = selectedInstitution
    override fun setSelectedInstitution(institution: String) {
        selectedInstitution = institution
    }

    override fun clearSession() { cookieJar.clear() }
    override fun logout() {
        stopTags(null)
        cookieJar.clear()
    }

    override fun stopTags(tags: String?) {
        val callList = ArrayList<Call>()
        callList.addAll(client.dispatcher.runningCalls())
        callList.addAll(client.dispatcher.queuedCalls())
        for (call in callList) {
            val local = call.request().tag()
            if ((local != null && local == tags) || tags == null) {
                call.cancel()
            }
        }
    }

    override fun getBase64Encoder() = base64Encoder

    override fun setCookiesOnClient(cookies: String) {
        val cookiesStr = cookies.split(";")
        val url = "http://academico2.uefs.br".toHttpUrl()
        val elements = cookiesStr.mapNotNull { Cookie.parse(url, it) }
        cookieJar.saveFromResponse(url, elements)
    }

    companion object {
        fun create(
            persist: CookiePersistor?,
            encoder: Base64Encoder,
            baseClient: OkHttpClient?
        ): SagresNavigator {
            return SagresNavigatorImpl(persist, encoder, baseClient)
        }
    }
}
