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

package com.forcetower.sagres.operation.document

import com.forcetower.sagres.Constants
import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresLinkFinder
import com.forcetower.sagres.request.SagresCalls
import okio.buffer
import okio.sink
import okio.source
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException

class DocumentOperation(
    private val file: File,
    private val endpoint: String,
    private val caller: SagresCalls
) : Operation<DocumentCallback> {

    override suspend fun execute(): DocumentCallback {
        val url = Constants.forInstitution(caller.selectedInstitution()).getUrl(endpoint)
        val call = caller.getPageCall(url)
        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val string = response.body!!.string()
                val document = string.asDocument()
                onFirstResponse(document)
            } else {
                DocumentCallback(Status.RESPONSE_FAILED).message("Load error").code(500)
            }
        } catch (e: IOException) {
            DocumentCallback(Status.NETWORK_ERROR).message(e.message).throwable(e)
        }
    }

    private suspend fun onFirstResponse(document: Document): DocumentCallback {
        val link = SagresLinkFinder.findLink(document)
        return if (link == null) {
            DocumentCallback(Status.RESPONSE_FAILED).code(600).message("Link not found")
        } else {
            downloadDocument(link)
        }
    }

    private suspend fun downloadDocument(link: String): DocumentCallback {
        return try {
            val call = caller.getPageCall(link)
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                if (file.exists()) file.delete()
                file.createNewFile()

                val sink = file.sink().buffer()
                sink.writeAll(response.body!!.source())
                sink.close()
                DocumentCallback(Status.SUCCESS).code(200)
            } else {
                DocumentCallback(Status.RESPONSE_FAILED).code(response.code).message("Error...")
            }
        } catch (e: IOException) {
            DocumentCallback(Status.NETWORK_ERROR).code(500).message(e.message).throwable(e)
        }
    }
}
