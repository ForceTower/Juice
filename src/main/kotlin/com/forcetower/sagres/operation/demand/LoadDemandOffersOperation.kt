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
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.parsers.SagresDemandParser
import com.forcetower.sagres.request.SagresCalls
import java.util.concurrent.Executor
import org.jsoup.nodes.Document
import timber.log.Timber
import timber.log.debug

class LoadDemandOffersOperation(executor: Executor?) : Operation<DemandOffersCallback>(executor) {
    init {
        perform()
    }

    override fun execute() {
        executeSteps()
    }

    private fun executeSteps() {
        val document = demandPage() ?: return
        val offers = SagresDemandParser.getOffers(document)
        if (offers != null) {
            publishProgress(DemandOffersCallback(Status.SUCCESS).offers(offers).document(document))
        } else {
            publishProgress(DemandOffersCallback(Status.APPROVAL_ERROR).message("Not able to find the demand object").document(document))
        }
    }

    private fun demandPage(): Document? {
        val call = SagresCalls.getDemandPage()

        try {
            val response = call.execute()
            if (response.isSuccessful) {
                Timber.debug { "Completed request!" }
                val body = response.body!!.string()
                return body.asDocument()
            } else {
                Timber.debug { "Failed loading" }
                publishProgress(DemandOffersCallback(Status.RESPONSE_FAILED).code(response.code).message("Failed loading"))
            }
        } catch (t: Throwable) {
            Timber.debug { "Error loading page. Throwable message ${t.message}" }
            publishProgress(DemandOffersCallback(Status.NETWORK_ERROR).throwable(t))
        }
        return null
    }
}
