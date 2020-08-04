package com.forcetower.sagres.operation.ohmyzsh

import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.request.SagresCalls
import java.util.concurrent.Executor

class JustDoIt(executor: Executor?) : Operation<DoneCallback>(executor) {
    init {
        perform()
    }

    override fun execute() {
        val call = SagresCalls.onMyZsH()
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                publishProgress(DoneCallback(Status.SUCCESS))
            } else {
                publishProgress(DoneCallback(Status.RESPONSE_FAILED))
            }
        } catch (error: Throwable) {
            publishProgress(DoneCallback(Status.NETWORK_ERROR).throwable(error))
        }
    }
}
