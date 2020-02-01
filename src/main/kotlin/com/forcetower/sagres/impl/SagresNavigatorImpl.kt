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

import androidx.annotation.RestrictTo
import com.forcetower.sagres.SagresNavigator
import com.forcetower.sagres.cookies.CookiePersistor
import com.forcetower.sagres.cookies.PersistentCookieJar
import com.forcetower.sagres.cookies.SetCookieCache
import com.forcetower.sagres.database.model.SagresCredential
import com.forcetower.sagres.database.model.SagresDemandOffer
import com.forcetower.sagres.decoders.Base64Encoder
import com.forcetower.sagres.executor.SagresTaskExecutor
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
import com.forcetower.sagres.operation.messages.MessagesOperation
import com.forcetower.sagres.operation.messages.OldMessagesOperation
import com.forcetower.sagres.operation.person.PersonCallback
import com.forcetower.sagres.operation.person.PersonOperation
import com.forcetower.sagres.operation.semester.SemesterCallback
import com.forcetower.sagres.operation.semester.SemesterOperation
import com.forcetower.sagres.operation.servicerequest.RequestedServicesCallback
import com.forcetower.sagres.operation.servicerequest.RequestedServicesOperation
import com.forcetower.sagres.persist.CachedPersistence
import io.reactivex.subjects.Subject
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.ConnectionSpec
import okhttp3.CookieJar
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import java.util.Arrays

class SagresNavigatorImpl private constructor(
    persist: CookiePersistor?,
    private val base64Encoder: Base64Encoder,
    private val persistence: CachedPersistence
) : SagresNavigator() {
    private val cookies = SetCookieCache()
    private val cookieJar = createCookieJar(cookies, persist)
    val client: OkHttpClient = createClient(cookieJar)
    private var selectedInstitution = "UEFS"

    private var credential: SagresCredential? = null

    private fun createClient(cookies: CookieJar): OkHttpClient {
        return OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
            .followRedirects(true)
            .cookieJar(cookies)
            .addInterceptor(createInterceptor())
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    private fun createInterceptor(): Interceptor {
        return Interceptor { chain ->
            val nRequest = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36")
                .build()

            val cred = credential
            if (cred == null) {
                chain.proceed(nRequest)
            } else {
                val credentials = Credentials.basic(cred.username, cred.password)
                if (nRequest.header("Authorization") != null) {
                    chain.proceed(nRequest)
                } else {
                    val cRequest = nRequest.newBuilder()
                        .addHeader("Authorization", credentials)
                        .addHeader("Accept", "application/json")
                        .build()

                    chain.proceed(cRequest)
                }
            }
        }
    }

    private fun createCookieJar(cookies: SetCookieCache, persist: CookiePersistor?): PersistentCookieJar {
        return PersistentCookieJar(cookies, persist)
    }

    override fun login(username: String, password: String): LoginCallback {
        return LoginOperation(username, password, null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun me(): PersonCallback {
        return PersonOperation(null, null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun messages(userId: Long, fetchAll: Boolean): MessagesCallback {
        return MessagesOperation(null, userId, fetchAll).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun messagesHtml(): MessagesCallback {
        return OldMessagesOperation(null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun calendar(): CalendarCallback {
        return CalendarOperation(null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun semesters(userId: Long): SemesterCallback {
        return SemesterOperation(null, userId).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun startPage(): StartPageCallback {
        return StartPageOperation(null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getCurrentGrades(): GradesCallback {
        return GradesOperation(null, null, null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getGradesFromSemester(semesterSagresId: Long, document: Document): GradesCallback {
        return GradesOperation(semesterSagresId, document, null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun downloadEnrollment(file: File): DocumentCallback {
        return DocumentOperation(file, "SAGRES_ENROLL_CERT", null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun downloadFlowchart(file: File): DocumentCallback {
        return DocumentOperation(file, "SAGRES_FLOWCHART", null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun downloadHistory(file: File): DocumentCallback {
        return DocumentOperation(file, "SAGRES_HISTORY", null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun loadDisciplineDetails(semester: String?, code: String?, group: String?, partialLoad: Boolean): DisciplineDetailsCallback {
        return DisciplineDetailsOperation(semester, code, group, partialLoad, null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun loadDemandOffers(): DemandOffersCallback {
        return LoadDemandOffersOperation(null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun createDemandOffer(offers: List<SagresDemandOffer>): DemandCreatorCallback {
        return CreateDemandOperation(offers, null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun getRequestedServices(): RequestedServicesCallback {
        return RequestedServicesOperation(null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun disciplinesExperimental(semester: String?, code: String?, group: String?, partialLoad: Boolean, discover: Boolean): FastDisciplinesCallback {
        return FastDisciplinesOperation(semester, code, group, partialLoad, discover, null).finishedResult
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aLogin(username: String, password: String): Subject<LoginCallback> {
        return LoginOperation(username, password, SagresTaskExecutor.networkThreadExecutor).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aMe(): Subject<PersonCallback> {
        return PersonOperation(null, SagresTaskExecutor.networkThreadExecutor).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aMessages(userId: Long, fetchAll: Boolean): Subject<MessagesCallback> {
        return MessagesOperation(SagresTaskExecutor.networkThreadExecutor, userId, fetchAll).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aMessagesHtml(needsAuth: Boolean): Subject<MessagesCallback> {
        return OldMessagesOperation(SagresTaskExecutor.networkThreadExecutor).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aCalendar(): Subject<CalendarCallback> {
        return CalendarOperation(SagresTaskExecutor.networkThreadExecutor).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aSemesters(userId: Long): Subject<SemesterCallback> {
        return SemesterOperation(SagresTaskExecutor.networkThreadExecutor, userId).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aStartPage(): Subject<StartPageCallback> {
        return StartPageOperation(SagresTaskExecutor.networkThreadExecutor).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aGetCurrentGrades(): Subject<GradesCallback> {
        return GradesOperation(null, null, SagresTaskExecutor.networkThreadExecutor).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aLoadDisciplineDetails(
        semester: String?,
        code: String?,
        group: String?,
        partialLoad: Boolean
    ): Subject<DisciplineDetailsCallback> {
        return DisciplineDetailsOperation(semester, code, group, partialLoad, SagresTaskExecutor.networkThreadExecutor).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aLoadDemandOffers(): Subject<DemandOffersCallback> {
        return LoadDemandOffersOperation(SagresTaskExecutor.networkThreadExecutor).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aGetRequestedServices(login: Boolean): Subject<RequestedServicesCallback> {
        return RequestedServicesOperation(SagresTaskExecutor.networkThreadExecutor).result
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun aDisciplinesExperimental(semester: String?, code: String?, group: String?, partialLoad: Boolean, discover: Boolean): Subject<FastDisciplinesCallback> {
        return FastDisciplinesOperation(semester, code, group, partialLoad, discover, SagresTaskExecutor.networkThreadExecutor).result
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

    override fun getCachingPersistence() = persistence

    override fun putCredentials(cred: SagresCredential?) {
        credential = cred
    }

    companion object {
        private lateinit var sDefaultInstance: SagresNavigatorImpl
        private val sLock = Any()

        val instance: SagresNavigatorImpl
            get() = synchronized(sLock) {
                if (::sDefaultInstance.isInitialized)
                    return sDefaultInstance
                else
                    throw IllegalStateException("Sagres navigator was not initialized")
            }

        fun initialize(
            persist: CookiePersistor?,
            encoder: Base64Encoder,
            persistence: CachedPersistence
        ) {
            synchronized(sLock) {
                if (!::sDefaultInstance.isInitialized) {
                    sDefaultInstance = SagresNavigatorImpl(persist, encoder, persistence)
                }
            }
        }
    }
}
