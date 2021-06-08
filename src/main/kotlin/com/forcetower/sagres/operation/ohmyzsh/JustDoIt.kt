package com.forcetower.sagres.operation.ohmyzsh

import com.forcetower.sagres.extension.executeSuspend
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.request.SagresCalls

class JustDoIt(private val caller: SagresCalls) : Operation<DoneCallback> {
    override suspend fun execute(): DoneCallback {
        val call = caller.onMyZsH()
        return try {
            val response = call.executeSuspend()
            if (response.isSuccessful) {
                DoneCallback(Status.SUCCESS)
            } else {
                DoneCallback(Status.RESPONSE_FAILED)
            }
        } catch (error: Throwable) {
            DoneCallback(Status.NETWORK_ERROR).throwable(error)
        }
    }
}
