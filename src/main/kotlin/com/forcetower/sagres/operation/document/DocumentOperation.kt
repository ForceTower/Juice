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
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresLinkFinder
import com.forcetower.sagres.request.SagresCalls
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor
import okio.buffer
import okio.sink
import org.jsoup.nodes.Document

class DocumentOperation(
    private val file: File,
    private val endpoint: String,
    executor: Executor?
) : Operation<DocumentCallback>(executor) {

    init {
        this.perform()
    }

    override fun execute() {
        val url = Constants.getUrl(endpoint)
        val call = SagresCalls.getPageCall(url)
        publishProgress(DocumentCallback(Status.LOADING))
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val string = response.body!!.string()
                val document = string.asDocument()
                onFirstResponse(document)
            } else {
                publishProgress(DocumentCallback(Status.RESPONSE_FAILED).message("Load error").code(500))
            }
        } catch (e: IOException) {
            publishProgress(DocumentCallback(Status.NETWORK_ERROR).message(e.message).throwable(e))
        }
    }

    private fun onFirstResponse(document: Document) {
        val link = SagresLinkFinder.findLink(document)
        if (link == null) {
            publishProgress(DocumentCallback(Status.RESPONSE_FAILED).code(600).message("Link not found"))
        } else {
            downloadDocument(link)
        }
    }

    private fun downloadDocument(link: String) {
        try {
            val call = SagresCalls.getPageCall(link)
            val response = call.execute()
            if (response.isSuccessful) {
                if (file.exists()) file.delete()
                file.createNewFile()

                val sink = file.sink().buffer()
                sink.writeAll(response.body!!.source())
                sink.close()
                publishProgress(DocumentCallback(Status.SUCCESS).code(200))
            } else {
                publishProgress(DocumentCallback(Status.RESPONSE_FAILED).code(response.code).message("Error..."))
            }
        } catch (e: IOException) {
            publishProgress(DocumentCallback(Status.NETWORK_ERROR).code(500).message(e.message).throwable(e))
        }
    }
}
