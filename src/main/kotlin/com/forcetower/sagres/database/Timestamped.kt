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

package com.forcetower.sagres.database

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import timber.log.Timber
import timber.log.error

interface Timestamped {

    @Throws(ParseException::class)
    fun getInMillis(string: String?): Long? {
        string ?: return null
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", Locale.getDefault())
            return formatter.parse(string.trim { it <= ' ' }).time
        } catch (e: Exception) {
            return try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
                formatter.parse(string.trim { it <= ' ' }).time
            } catch (e1: Exception) {
                try {
                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.getDefault())
                    formatter.parse(string.trim { it <= ' ' }).time
                } catch (e2: Exception) {
                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
                    formatter.parse(string.trim { it <= ' ' }).time
                }
            }
        }
    }

    fun getInMillis(string: String?, def: Long): Long {
        string ?: return def
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mmX", Locale.getDefault())
            return formatter.parse(string.trim { it <= ' ' }).time
        } catch (e: Exception) {
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault())
                return formatter.parse(string.trim { it <= ' ' }).time
            } catch (e1: Exception) {
                return try {
                    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.getDefault())
                    formatter.parse(string.trim { it <= ' ' }).time
                } catch (e2: Exception) {
                    try {
                        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
                        formatter.parse(string.trim { it <= ' ' }).time
                    } catch (e3: Exception) {
                        Timber.error { "Error while parsing data! Exception is ${e.message}" }
                        def
                    }
                }
            }
        }
    }
}
