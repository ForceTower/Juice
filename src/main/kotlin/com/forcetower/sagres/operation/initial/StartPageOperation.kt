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

package com.forcetower.sagres.operation.initial

import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresBasicParser
import com.forcetower.sagres.parsers.SagresCalendarParser
import com.forcetower.sagres.parsers.SagresDcpGroupsParser
import com.forcetower.sagres.parsers.SagresDisciplineParser
import com.forcetower.sagres.parsers.SagresMessageParser
import com.forcetower.sagres.parsers.SagresScheduleParser
import com.forcetower.sagres.parsers.SagresSemesterParser
import com.forcetower.sagres.request.SagresCalls
import org.jsoup.nodes.Document
import java.io.IOException

class StartPageOperation(
    private val caller: SagresCalls
) : Operation<StartPageCallback> {
    override suspend fun execute(): StartPageCallback {
        val call = caller.startPage
        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val document = body.asDocument()
                successMeasures(document)
            } else {
                StartPageCallback(Status.RESPONSE_FAILED).message(response.message).code(response.code)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            StartPageCallback(Status.NETWORK_ERROR).throwable(e)
        }
    }

    private fun successMeasures(document: Document): StartPageCallback {
        val calendar = SagresCalendarParser.getCalendar(document)
        val semesters = SagresSemesterParser.getSemesters(document)
        val disciplines = SagresDisciplineParser.getDisciplines(document)
        val groups = SagresDcpGroupsParser.getGroups(document)
        val locations = SagresScheduleParser.getSchedule(document)
        val messages = SagresMessageParser.getMessages(document)
        val demandOpen = SagresBasicParser.isDemandOpen(document)

        return StartPageCallback(Status.SUCCESS)
            .document(document)
            .calendar(calendar)
            .semesters(semesters)
            .disciplines(disciplines)
            .groups(groups)
            .locations(locations)
            .demandOpen(demandOpen)
            .messages(messages)
    }
}
