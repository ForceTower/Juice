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

package com.forcetower.sagres.operation

import org.jsoup.nodes.Document

@Suppress("UNCHECKED_CAST")
abstract class BaseCallback<out T : BaseCallback<T>>(val status: Status) {
    var message: String? = null
        private set
    var code: Int = 0
        private set
    var throwable: Throwable? = null
        private set
    var document: Document? = null
        private set

    fun message(message: String?): T {
        this.message = message
        return this as T
    }

    fun code(code: Int): T {
        this.code = code
        return this as T
    }

    fun throwable(throwable: Throwable?): T {
        this.throwable = throwable
        return this as T
    }

    fun document(document: Document?): T {
        this.document = document
        return this as T
    }
}
