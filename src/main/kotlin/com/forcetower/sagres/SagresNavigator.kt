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

package com.forcetower.sagres

import com.forcetower.sagres.cookies.CookiePersistor
import com.forcetower.sagres.database.model.SagresCredential
import com.forcetower.sagres.database.model.SagresDemandOffer
import com.forcetower.sagres.decoders.Base64Encoder
import com.forcetower.sagres.impl.ApacheBase64Encoder
import com.forcetower.sagres.impl.InMemoryCachePersistence
import com.forcetower.sagres.impl.SagresNavigatorImpl
import com.forcetower.sagres.operation.calendar.CalendarCallback
import com.forcetower.sagres.operation.demand.DemandCreatorCallback
import com.forcetower.sagres.operation.demand.DemandOffersCallback
import com.forcetower.sagres.operation.disciplinedetails.DisciplineDetailsCallback
import com.forcetower.sagres.operation.disciplines.FastDisciplinesCallback
import com.forcetower.sagres.operation.document.DocumentCallback
import com.forcetower.sagres.operation.grades.GradesCallback
import com.forcetower.sagres.operation.initial.StartPageCallback
import com.forcetower.sagres.operation.login.LoginCallback
import com.forcetower.sagres.operation.messages.MessagesCallback
import com.forcetower.sagres.operation.person.PersonCallback
import com.forcetower.sagres.operation.semester.SemesterCallback
import com.forcetower.sagres.operation.servicerequest.RequestedServicesCallback
import com.forcetower.sagres.persist.CachedPersistence
import io.reactivex.subjects.Subject
import java.io.File
import org.jsoup.nodes.Document

abstract class SagresNavigator {
    abstract fun login(username: String, password: String): LoginCallback
    abstract fun me(): PersonCallback
    abstract fun messages(userId: Long, fetchAll: Boolean = false): MessagesCallback
    abstract fun messagesHtml(): MessagesCallback
    abstract fun calendar(): CalendarCallback
    abstract fun semesters(userId: Long): SemesterCallback
    abstract fun startPage(): StartPageCallback
    abstract fun getCurrentGrades(): GradesCallback
    abstract fun getGradesFromSemester(semesterSagresId: Long, document: Document): GradesCallback
    abstract fun downloadHistory(file: File): DocumentCallback
    abstract fun downloadEnrollment(file: File): DocumentCallback
    abstract fun downloadFlowchart(file: File): DocumentCallback
    abstract fun loadDisciplineDetails(semester: String?, code: String?, group: String?, partialLoad: Boolean = false): DisciplineDetailsCallback
    abstract fun loadDemandOffers(): DemandOffersCallback
    abstract fun createDemandOffer(offers: List<SagresDemandOffer>): DemandCreatorCallback
    abstract fun getRequestedServices(): RequestedServicesCallback
    abstract fun disciplinesExperimental(semester: String? = null, code: String? = null, group: String? = null, partialLoad: Boolean = false, discover: Boolean = true): FastDisciplinesCallback

    abstract fun aLogin(username: String, password: String): Subject<LoginCallback>
    abstract fun aMe(): Subject<PersonCallback>
    abstract fun aMessages(userId: Long, fetchAll: Boolean = false): Subject<MessagesCallback>
    abstract fun aMessagesHtml(needsAuth: Boolean = false): Subject<MessagesCallback>
    abstract fun aCalendar(): Subject<CalendarCallback>
    abstract fun aSemesters(userId: Long): Subject<SemesterCallback>
    abstract fun aStartPage(): Subject<StartPageCallback>
    abstract fun aGetCurrentGrades(): Subject<GradesCallback>
    abstract fun aLoadDisciplineDetails(semester: String?, code: String?, group: String?, partialLoad: Boolean = false): Subject<DisciplineDetailsCallback>
    abstract fun aLoadDemandOffers(): Subject<DemandOffersCallback>
    abstract fun aGetRequestedServices(login: Boolean = false): Subject<RequestedServicesCallback>
    abstract fun aDisciplinesExperimental(semester: String? = null, code: String? = null, group: String? = null, partialLoad: Boolean = false, discover: Boolean = true): Subject<FastDisciplinesCallback>

    abstract fun getSelectedInstitution(): String
    abstract fun setSelectedInstitution(institution: String)
    abstract fun clearSession()
    abstract fun logout()
    abstract fun stopTags(tags: String?)

    abstract fun getBase64Encoder(): Base64Encoder
    abstract fun getCachingPersistence(): CachedPersistence
    abstract fun putCredentials(cred: SagresCredential?)

    companion object {
        @JvmStatic
        val instance: SagresNavigator
            get() = SagresNavigatorImpl.instance

        @JvmOverloads
        @JvmStatic
        fun initialize(
            persist: CookiePersistor? = null,
            institution: String = "UEFS",
            // Android implementation of this is different, so we need to adapt
            // The baseline is to use Apache things
            base64Encoder: Base64Encoder = ApacheBase64Encoder(),
            persistence: CachedPersistence = InMemoryCachePersistence()
        ) {
            SagresNavigatorImpl.initialize(persist, base64Encoder, persistence)
            SagresNavigator.instance.setSelectedInstitution(institution)
        }

        @JvmStatic
        fun getSupportedInstitutions() = Constants.SUPPORTED_INSTITUTIONS
    }
}
