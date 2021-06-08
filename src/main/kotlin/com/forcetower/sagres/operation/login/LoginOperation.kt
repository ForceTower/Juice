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

package com.forcetower.sagres.operation.login

import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresBasicParser
import com.forcetower.sagres.request.SagresCalls
import com.forcetower.sagres.utils.ConnectedStates
import okhttp3.Response
import org.jsoup.nodes.Document
import java.io.IOException

class LoginOperation constructor(
    private val username: String,
    private val password: String,
    private val gresp: String?,
    private val caller: SagresCalls
) : Operation<LoginCallback> {
    override suspend fun execute(): LoginCallback {
        val call = caller.login(username, password, gresp)

        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val body = response.body ?: throw IOException("empty body")
                val string = body.string()
                resolveLogin(string, response)
            } else {
                val body = response.body!!.string()
                val doc = body.asDocument()
                LoginCallback.Builder(Status.RESPONSE_FAILED).document(doc).code(response.code).build()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            LoginCallback.Builder(Status.NETWORK_ERROR).throwable(e).build()
        }
    }

    private suspend fun resolveLogin(string: String, response: Response): LoginCallback {
        val document = string.asDocument()

        return when (SagresBasicParser.isConnected(document)) {
            ConnectedStates.CONNECTED -> continueWithResolve(document, response)
            ConnectedStates.INVALID -> continueWithInvalidation(document)
            ConnectedStates.SESSION_TIMEOUT -> continueWithStopFlags(document)
            ConnectedStates.UNKNOWN -> continueWithUnknownFlags(document)
        }
    }

    private fun continueWithUnknownFlags(document: Document): LoginCallback {
        return LoginCallback.Builder(Status.INVALID_LOGIN).code(500).document(document).build()
    }

    private fun continueWithStopFlags(document: Document): LoginCallback {
        return LoginCallback.Builder(Status.INVALID_LOGIN).code(440).document(document).build()
    }

    private fun continueWithInvalidation(document: Document): LoginCallback {
        return LoginCallback.Builder(Status.INVALID_LOGIN).code(401).document(document).build()
    }

    private suspend fun continueWithResolve(document: Document, response: Response): LoginCallback {
        return if (SagresBasicParser.needApproval(document)) {
            approval(document, response)
        } else {
            successMeasures(document)
        }
    }

    private suspend fun approval(approvalDocument: Document, oldResp: Response): LoginCallback {
        val call = caller.loginApproval(approvalDocument, oldResp)
        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val document = response.body!!.string().asDocument()
                successMeasures(document)
            } else {
                LoginCallback.Builder(Status.APPROVAL_ERROR).code(response.code).build()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            LoginCallback.Builder(Status.NETWORK_ERROR).throwable(e).build()
        }
    }

    private fun successMeasures(document: Document): LoginCallback {
        return LoginCallback.Builder(Status.SUCCESS).document(document).build()
    }
}
