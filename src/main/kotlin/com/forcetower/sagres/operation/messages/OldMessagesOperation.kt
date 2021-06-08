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

package com.forcetower.sagres.operation.messages

import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresMessageParser
import com.forcetower.sagres.request.SagresCalls

class OldMessagesOperation(private val caller: SagresCalls) : Operation<MessagesCallback> {
    override suspend fun execute(): MessagesCallback {
        val call = caller.startPage
        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                processResponse(response.body!!.string())
            } else {
                MessagesCallback(Status.RESPONSE_FAILED)
            }
        } catch (e: Exception) {
            MessagesCallback(Status.NETWORK_ERROR)
        }
    }

    private fun processResponse(response: String): MessagesCallback {
        val document = response.asDocument()
        val messages = SagresMessageParser.getMessages(document)
        messages.reversed().forEachIndexed { index, message ->
            message.processingTime = System.currentTimeMillis() + index
        }
        return MessagesCallback(Status.SUCCESS).messages(messages)
    }
}
