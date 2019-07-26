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

package com.forcetower.sagres.impl

import com.forcetower.sagres.SagresNavigator
import com.forcetower.sagres.cookies.CookiePersistor
import com.forcetower.sagres.cookies.PersistentCookieJar
import com.forcetower.sagres.cookies.SetCookieCache
import com.forcetower.sagres.operation.document.DocumentCallback
import com.forcetower.sagres.operation.document.DocumentOperation
import com.forcetower.sagres.operation.login.LoginCallback
import com.forcetower.sagres.operation.login.LoginOperation
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class SagresNavigatorImpl private constructor(
    persist: CookiePersistor
) : SagresNavigator() {
    private val cookies = SetCookieCache()
    private val cookieJar = createCookieJar(cookies, persist)
    val client: OkHttpClient = createClient(cookieJar)

    private fun createClient(cookies: CookieJar): OkHttpClient {
        return OkHttpClient.Builder()
            .followRedirects(true)
            .cookieJar(cookies)
            .addInterceptor(createInterceptor())
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    private fun createInterceptor(): Interceptor {
        return Interceptor {
            it.proceed(it.request())
        }
    }

    private fun createCookieJar(cookies: SetCookieCache, persist: CookiePersistor): CookieJar {
        return PersistentCookieJar(cookies, persist)
    }

    override suspend fun login(username: String, password: String): LoginCallback {
        return LoginOperation(username, password, null).finishedResult
    }

    override suspend fun downloadHistory(file: File): DocumentCallback {
        return DocumentOperation(file, "SAGRES_HISTORY", null).finishedResult
    }

    override fun getSelectedInstitution() = "UEFS"
    override fun setSelectedInstitution(institution: String) = Unit
    override fun clearSession() { cookies.clear() }

    companion object {
        private lateinit var sDefaultInstance: SagresNavigatorImpl
        private val sLock = Any()

        val instance: SagresNavigatorImpl
            get() = synchronized(sLock) {
                if (::sDefaultInstance.isInitialized)
                    return sDefaultInstance
                else
                    throw IllegalStateException("Sagres navigator was not initialized")
            }

        fun initialize(persist: CookiePersistor) {
            synchronized(sLock) {
                if (!::sDefaultInstance.isInitialized) {
                    sDefaultInstance = SagresNavigatorImpl(persist)
                }
            }
        }
    }
}