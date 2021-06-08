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

package com.forcetower.sagres.operation.demand

import com.forcetower.sagres.database.model.SagresDemandOffer
import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.request.SagresCalls
import org.jsoup.nodes.Document

class CreateDemandOperation(
    private val revised: List<SagresDemandOffer>,
    private val caller: SagresCalls
) : Operation<DemandCreatorCallback> {

    override suspend fun execute(): DemandCreatorCallback {
        val callback = loadOffers()
        if (callback.status != Status.SUCCESS) return DemandCreatorCallback.copyFrom(callback)

        val document = callback.document!!
        val offers = callback.getOffers()!!
        val hash = offers.groupBy { it.code }

        revised.forEach {
            val list = hash[it.code]
            if (list == null || list.size != 1) {
                return DemandCreatorCallback(Status.APPROVAL_ERROR).message("${it.code} was not identified on second pass. List size: ${list?.size}")
            }
            list[0].selected = it.selected
        }

        val list = hash.map { it.value[0] }
        val call = caller.createDemand(list, document)
        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val complete = body.asDocument()
                finalSteps(complete)
            } else {
                DemandCreatorCallback(Status.RESPONSE_FAILED).code(response.code)
            }
        } catch (t: Throwable) {
            DemandCreatorCallback(Status.NETWORK_ERROR).throwable(t)
        }
    }

    private suspend fun loadOffers(): DemandOffersCallback {
        val callback = LoadDemandOffersOperation(caller).execute()
        val offers = callback.getOffers()
        val document = callback.document
        return when {
            callback.status != Status.SUCCESS -> {
                DemandOffersCallback.copyFrom(callback)
            }
            offers == null || document == null -> {
                DemandOffersCallback(Status.UNKNOWN_FAILURE).message("Load demand had a null response")
            }
            else -> callback
        }
    }

    private fun finalSteps(complete: Document): DemandCreatorCallback {
        val element = complete.selectFirst("span[class=\"msg-sucesso anim-fadeIn\"]")
        return if (element != null) {
            val text = element.text().trim()
            if (text.contains("O registro foi atualizado com sucesso", ignoreCase = true)) {
                DemandCreatorCallback(Status.SUCCESS).message(text)
            } else {
                DemandCreatorCallback(Status.COMPLETED).message(text)
            }
        } else {
            DemandCreatorCallback(Status.UNKNOWN_FAILURE).message("Success message not found. It's possible it failed")
        }
    }
}
