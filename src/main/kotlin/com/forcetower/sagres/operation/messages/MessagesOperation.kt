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

import com.forcetower.sagres.SagresNavigator
import com.forcetower.sagres.database.model.SagresClass
import com.forcetower.sagres.database.model.SagresDisciplineResumed
import com.forcetower.sagres.database.model.SagresLinker
import com.forcetower.sagres.database.model.SagresMessage
import com.forcetower.sagres.database.model.SagresMessageScope
import com.forcetower.sagres.database.model.SagresPerson
import com.forcetower.sagres.operation.Dumb
import com.forcetower.sagres.operation.Operation
import com.forcetower.sagres.operation.Status
import com.forcetower.sagres.request.SagresCalls
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.util.ArrayList
import java.util.concurrent.Executor

class MessagesOperation(
    executor: Executor?,
    private val userId: Long,
    private val fetchAll: Boolean
) : Operation<MessagesCallback>(executor) {
    init {
        this.perform()
    }

    override fun execute() {
        executor()
    }

    fun executor() {
        publishProgress(MessagesCallback(Status.STARTED))
        val call = SagresCalls.getMessages(userId)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val result = mutableListOf<SagresMessage>()
                val body = response.body!!.string()
                val first = Gson().fromJson(body, MessageResponse::class.java)
                result += first.messages
                if (fetchAll) {
                    val messages = fetchAllMessages(first)
                    result += messages
                }
                successMeasures(result)
            } else {
                publishProgress(MessagesCallback(Status.RESPONSE_FAILED).code(response.code).message(response.message))
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            publishProgress(MessagesCallback(Status.NETWORK_ERROR).throwable(t))
        }
    }

    private fun fetchAllMessages(first: MessageResponse): List<SagresMessage> {
        val result = mutableListOf<SagresMessage>()
        var nextLink = first.older
        var link = nextLink?.getLink()
        try {
            while (link != null) {
                val call = SagresCalls.getLink(link)
                val response = call.execute()
                if (response.isSuccessful) {
                    val body = response.body!!.string()
                    val value = Gson().fromJson(body, MessageResponse::class.java)
                    result += value.messages
                    nextLink = value.older
                    link = nextLink?.getLink()
                } else {
                    link = null
                }
            }
        } catch (t: Throwable) {
        }
        return result
    }

    private fun successMeasures(messages: List<SagresMessage>) {
        try {
            val items = messages.toMutableList()
            items.sort()
            items.forEach { extractDetailsIfNeeded(it) }
            publishProgress(MessagesCallback(Status.SUCCESS).messages(items))
        } catch (t: Throwable) {
            publishProgress(MessagesCallback(Status.UNKNOWN_FAILURE).throwable(t).message(t.message))
        }
    }

    private fun successMeasures(body: String) {
        try {
            val type = object : TypeToken<Dumb<List<SagresMessage>>>() {}.type
            val dMessages = Gson().fromJson<Dumb<List<SagresMessage>>>(body, type)
            val items = dMessages.items.sorted()
            for (message in items) {
                extractDetailsIfNeeded(message)
            }

            publishProgress(MessagesCallback(Status.SUCCESS).messages(items))
        } catch (t: Throwable) {
            publishProgress(MessagesCallback(Status.UNKNOWN_FAILURE).throwable(t).message(body))
        }
    }

    private fun extractDetailsIfNeeded(message: SagresMessage) {
        val linker = message.sender ?: return
        val person = getPerson(linker)
        if (person != null)
            message.senderName = person.name
        else {
            if (message.senderProfile == 3) {
                message.senderName = ".UEFS."
            }
        }

        // Message is from a teacher
        if (message.senderProfile == 2) {
            val scopes = message.scopes
            scopes ?: return
            val scope = getScope(scopes)
            if (scope != null) {
                val clazz = getClazz(scope)
                if (clazz != null) {
                    val discipline = getDiscipline(clazz)
                    if (discipline != null) {
                        message.discipline = discipline.name
                        message.disciplineCode = discipline.code
                        message.objective = discipline.objective
                    }
                }
            }
        }
    }

    private fun getPerson(linker: SagresLinker): SagresPerson? {
        val href = linker.getLink() ?: return null

        val persistence = SagresNavigator.instance.getCachingPersistence()
        val cached = persistence.person().retrieveFromLink(href)
        if (cached != null) return cached

        val call = SagresCalls.getLink(href)

        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                return gson.fromJson(body, SagresPerson::class.java).apply {
                    link = href
                }.also {
                    persistence.person().save("${it.id}", it)
                }
            }
        } catch (e: Exception) {
        }

        return null
    }

    private fun getScope(scopes: SagresLinker): SagresMessageScope? {
        val link = scopes.getLink() ?: return null

        val persistence = SagresNavigator.instance.getCachingPersistence()
        val cached = persistence.messageScope().retrieve(link)
        if (cached != null) return cached

        val call = SagresCalls.getLink(link)

        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val token = object : TypeToken<Dumb<ArrayList<SagresMessageScope>>>() {}.type
                val scoping = gson.fromJson<Dumb<ArrayList<SagresMessageScope>>>(body, token)
                val items = scoping.items
                if (items.isNotEmpty()) {
                    val scoped = items[0]
                    val linker = scoped.clazzLinker
                    if (linker != null) {
                        scoped.clazzLink = linker.getLink()
                        scoped.uid = link
                    }
                    persistence.messageScope().save(scoped.uid, scoped)
                    return scoped
                }
            }
        } catch (e: Exception) {
        }

        return null
    }

    private fun getClazz(scope: SagresMessageScope): SagresClass? {
        val link = scope.clazzLink ?: return null

        val persistence = SagresNavigator.instance.getCachingPersistence()
        val cached = persistence.clazz().retrieveFromLink(link)
        if (cached != null) return cached

        val call = SagresCalls.getLink(link)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val clazz = gson.fromJson(body, SagresClass::class.java)
                val discipline = clazz.discipline
                if (discipline != null) {
                    clazz.disciplineLink = discipline.getLink()
                    clazz.link = link
                }
                persistence.clazz().save("${clazz.id}", clazz)
                return clazz
            }
        } catch (e: Exception) {
        }

        return null
    }

    private fun getDiscipline(clazz: SagresClass): SagresDisciplineResumed? {
        val link = clazz.disciplineLink ?: return null

        val persistence = SagresNavigator.instance.getCachingPersistence()
        val cached = persistence.disciplineResumed().retrieveFromLink(link)
        if (cached != null) return cached

        val call = SagresCalls.getLink(link)
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                val body = response.body!!.string()
                val disciplined = gson.fromJson(body, SagresDisciplineResumed::class.java).apply {
                    code = code?.trim()
                    name = name?.trim()
                    resumedName = resumedName?.trim()
                    objective = objective?.trim()
                }

                val department = disciplined.department
                if (department != null) {
                    disciplined.departmentLink = department.getLink()
                    disciplined.link = link
                }
                persistence.disciplineResumed().save("${disciplined.id}", disciplined)
                return disciplined
            }
        } catch (e: Exception) {
        }

        return null
    }

    private data class MessageResponse(
        @SerializedName("maisAntigos")
        var older: SagresLinker? = null,
        @SerializedName("maisRecentes")
        var newer: SagresLinker? = null,
        @SerializedName("itens")
        var messages: List<SagresMessage> = emptyList()
    )
}
