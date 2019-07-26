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

package com.forcetower.sagres

import com.forcetower.sagres.cookies.CookiePersistor
import com.forcetower.sagres.impl.SagresNavigatorImpl
import com.forcetower.sagres.operation.document.DocumentCallback
import com.forcetower.sagres.operation.login.LoginCallback
import java.io.File

abstract class SagresNavigator {
    abstract suspend fun login(username: String, password: String): LoginCallback
    abstract suspend fun downloadHistory(file: File): DocumentCallback
    abstract fun getSelectedInstitution(): String
    abstract fun setSelectedInstitution(institution: String)
    abstract fun clearSession()

    companion object {
        val instance: SagresNavigator
            get() = SagresNavigatorImpl.instance

        fun initialize(persist: CookiePersistor) {
            SagresNavigatorImpl.initialize(persist)
        }

        fun getSupportedInstitutions() = Constants.SUPPORTED_INSTITUTIONS
    }
}