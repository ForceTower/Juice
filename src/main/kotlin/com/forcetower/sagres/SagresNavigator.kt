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
import com.forcetower.sagres.decoders.Base64Encoder
import com.forcetower.sagres.impl.ApacheBase64Encoder
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
import com.forcetower.sagres.operation.ohmyzsh.DoneCallback
import com.forcetower.sagres.operation.servicerequest.RequestedServicesCallback
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document
import java.io.File

abstract class SagresNavigator {
    abstract suspend fun login(username: String, password: String, gresp: String? = null): LoginCallback
    abstract suspend fun messagesHtml(): MessagesCallback
    abstract suspend fun calendar(): CalendarCallback
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
    abstract suspend fun ohMyZsh(): DoneCallback

    abstract fun getSelectedInstitution(): String
    abstract fun setSelectedInstitution(institution: String)
    abstract fun clearSession()
    abstract fun logout()
    abstract fun stopTags(tags: String?)

    abstract fun getBase64Encoder(): Base64Encoder

    abstract fun setCookiesOnClient(cookies: String)

    companion object {
        @JvmOverloads
        @JvmStatic
        fun initialize(
            persist: CookiePersistor? = null,
            institution: String = "UEFS",
            // Android implementation of this is different, so we need to adapt
            // The baseline is to use Apache things
            base64Encoder: Base64Encoder = ApacheBase64Encoder(),
            baseClient: OkHttpClient? = null
        ): SagresNavigator {
            val instance = SagresNavigatorImpl.create(persist, base64Encoder, baseClient)
            instance.setSelectedInstitution(institution)
            return instance
        }

        @JvmStatic
        fun getSupportedInstitutions() = Constants.SUPPORTED_INSTITUTIONS
    }
}
