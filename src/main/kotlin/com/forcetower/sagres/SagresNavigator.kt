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
import com.forcetower.sagres.database.model.SagresDemandOffer
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
import org.jsoup.nodes.Document
import java.io.File

abstract class SagresNavigator {
    abstract suspend fun login(username: String, password: String): LoginCallback
    abstract suspend fun me(): PersonCallback
    abstract suspend fun messages(userId: Long, fetchAll: Boolean = false): MessagesCallback
    abstract suspend fun messagesHtml(): MessagesCallback
    abstract suspend fun calendar(): CalendarCallback
    abstract suspend fun semesters(userId: Long): SemesterCallback
    abstract suspend fun startPage(): StartPageCallback
    abstract suspend fun getCurrentGrades(): GradesCallback
    abstract suspend fun getGradesFromSemester(semesterSagresId: Long, document: Document): GradesCallback
    abstract suspend fun downloadHistory(file: File): DocumentCallback
    abstract suspend fun downloadEnrollment(file: File): DocumentCallback
    abstract suspend fun downloadFlowchart(file: File): DocumentCallback
    abstract suspend fun loadDisciplineDetails(semester: String?, code: String?, group: String?, partialLoad: Boolean = false): DisciplineDetailsCallback
    abstract suspend fun loadDemandOffers(): DemandOffersCallback
    abstract suspend fun createDemandOffer(offers: List<SagresDemandOffer>): DemandCreatorCallback
    abstract suspend fun getRequestedServices(): RequestedServicesCallback
    abstract suspend fun disciplinesExperimental(semester: String? = null, code: String? = null, group: String? = null, partialLoad: Boolean = false, discover: Boolean = true): FastDisciplinesCallback

//    abstract fun aLogin(username: String, password: String): Subject<LoginCallback>
//    abstract fun aMe(): Subject<PersonCallback>
//    abstract fun aMessages(userId: Long, fetchAll: Boolean = false): Subject<MessagesCallback>
//    abstract fun aMessagesHtml(needsAuth: Boolean = false): Subject<MessagesCallback>
//    abstract fun aCalendar(): Subject<CalendarCallback>
//    abstract fun aSemesters(userId: Long): Subject<SemesterCallback>
//    abstract fun aStartPage(): Subject<StartPageCallback>
//    abstract fun aGetCurrentGrades(): Subject<GradesCallback>
//    abstract fun aLoadDisciplineDetails(semester: String?, code: String?, group: String?, partialLoad: Boolean = false): Subject<DisciplineDetailsCallback>
//    abstract fun aLoadDemandOffers(): Subject<DemandOffersCallback>
//    abstract fun aGetRequestedServices(login: Boolean = false): Subject<RequestedServicesCallback>
    
    abstract fun getSelectedInstitution(): String
    abstract fun setSelectedInstitution(institution: String)
    abstract fun clearSession()
    abstract fun logout()
    abstract fun stopTags(tags: String?)

    companion object {
        val instance: SagresNavigator
            get() = SagresNavigatorImpl.instance

        fun initialize(persist: CookiePersistor?) {
            SagresNavigatorImpl.initialize(persist)
        }

        fun getSupportedInstitutions() = Constants.SUPPORTED_INSTITUTIONS
    }
}