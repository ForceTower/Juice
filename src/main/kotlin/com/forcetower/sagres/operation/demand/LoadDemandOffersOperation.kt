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

import com.forcetower.sagres.extension.asDocument
import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresDemandParser
import com.forcetower.sagres.request.SagresCalls
import org.jsoup.nodes.Document

class LoadDemandOffersOperation(private val caller: SagresCalls) : Operation<DemandOffersCallback> {
    override suspend fun execute(): DemandOffersCallback {
        val (document, callback) = demandPage()
        if (document == null) return callback!!

        val offers = SagresDemandParser.getOffers(document)
        return if (offers != null) {
            DemandOffersCallback(Status.SUCCESS).offers(offers).document(document)
        } else {
            DemandOffersCallback(Status.APPROVAL_ERROR).message("Not able to find the demand object").document(document)
        }
    }

    private suspend fun demandPage(): Pair<Document?, DemandOffersCallback?> {
        val call = caller.getDemandPage()

        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return body.asDocument() to null
            } else {
                null to DemandOffersCallback(Status.RESPONSE_FAILED).code(response.code).message("Failed loading")
            }
        } catch (t: Throwable) {
            null to DemandOffersCallback(Status.NETWORK_ERROR).throwable(t)
        }
    }
}
