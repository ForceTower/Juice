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

package com.forcetower.sagres.cookies

import java.util.HashSet
import okhttp3.Cookie

class SetCookieCache : CookieCache {
    private val cookies: MutableSet<IdentifiableCookie>

    init {
        cookies = HashSet()
    }

    fun findCookie(name: String): Cookie? {
        for (identifiable in this.cookies) {
            val cookie = identifiable.cookie
            if (cookie.name.equals(name, ignoreCase = true)) {
                return cookie
            }
        }
        return null
    }

    override fun addAll(cookies: Collection<Cookie>) {
        for (cookie in IdentifiableCookie.decorateAll(cookies)) {
            this.cookies.remove(cookie)
            this.cookies.add(cookie)
        }
    }

    override fun clear() {
        cookies.clear()
    }

    override fun iterator(): MutableIterator<Cookie> {
        return SetCookieCacheIterator()
    }

    private inner class SetCookieCacheIterator : MutableIterator<Cookie> {
        private val iterator: MutableIterator<IdentifiableCookie> = cookies.iterator()

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }

        override fun next(): Cookie {
            return iterator.next().cookie
        }

        override fun remove() {
            iterator.remove()
        }
    }
}
