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

import java.util.ArrayList
import okhttp3.Cookie

/**
 * This class decorates a Cookie to re-implements equals() and hashcode() methods in order to identify
 * the cookie by the following attributes: name, domain, path, secure & hostOnly.
 *
 * This new behaviour will be useful in determining when an already existing cookie in session must be overwritten.
 */
internal class IdentifiableCookie private constructor(val cookie: Cookie) {
    override fun equals(other: Any?): Boolean {
        if (other !is IdentifiableCookie) return false
        return (other.cookie.name == this.cookie.name &&
                other.cookie.domain == this.cookie.domain &&
                other.cookie.path == this.cookie.path &&
                other.cookie.secure == this.cookie.secure &&
                other.cookie.hostOnly == this.cookie.hostOnly)
    }

    override fun hashCode(): Int {
        var hash = 17
        hash = 31 * hash + cookie.name.hashCode()
        hash = 31 * hash + cookie.domain.hashCode()
        hash = 31 * hash + cookie.path.hashCode()
        hash = 31 * hash + if (cookie.secure) 0 else 1
        hash = 31 * hash + if (cookie.hostOnly) 0 else 1
        return hash
    }

    companion object {
        fun decorateAll(cookies: Collection<Cookie>): List<IdentifiableCookie> {
            val identifiableCookies = ArrayList<IdentifiableCookie>(cookies.size)
            for (cookie in cookies) {
                identifiableCookies.add(IdentifiableCookie(cookie))
            }
            return identifiableCookies
        }
    }
}
