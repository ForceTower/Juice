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

package com.forcetower.sagres.utils

/**
 * Created by João Paulo on 06/03/2018.
 */

object WordUtils {
    fun toTitleCase(str: String?): String? {
        str ?: return null

        val givenString = str.toLowerCase()

        val arr = givenString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sb = StringBuilder()

        for (i in arr.indices) {
            val anArr = arr[i]

            if (i == arr.size - 1 && anArr.length <= 2) {
                sb.append(anArr.toUpperCase())
                continue
            }

            if (isGreekOneToTen(anArr)) {
                sb.append(anArr.toUpperCase()).append(" ")
                continue
            }

            // Special case only for "MI's". PBL!!!!
            if (anArr.equals("MI", ignoreCase = true)) {
                sb.append(anArr.toUpperCase()).append(" ")
                continue
            }

            if (anArr.length < 3 && !anArr.endsWith(".") || anArr.length == 3 && anArr.endsWith("s")) {
                sb.append(anArr).append(" ")
                continue
            }

            sb.append(Character.toUpperCase(anArr[0]))
                .append(anArr.substring(1)).append(" ")
        }
        return sb.toString().trim()
    }

    private fun isGreekOneToTen(str: String): Boolean {
        return str.equals("i", ignoreCase = true) ||
                str.equals("ii", ignoreCase = true) ||
                str.equals("iii", ignoreCase = true) ||
                str.equals("iv", ignoreCase = true) ||
                str.equals("v", ignoreCase = true) ||
                str.equals("vi", ignoreCase = true) ||
                str.equals("vii", ignoreCase = true) ||
                str.equals("viii", ignoreCase = true) ||
                str.equals("ix", ignoreCase = true) ||
                str.equals("x", ignoreCase = true)
    }

    fun capitalize(str: String?): String? {
        str ?: return null

        val givenString = str.toLowerCase()

        val arr = givenString.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sb = StringBuilder()

        for (anArr in arr) {
            if (anArr.isEmpty()) continue
            if (anArr.length < 2) {
                sb.append(anArr).append(" ")
            } else {
                sb.append(Character.toUpperCase(anArr[0]))
                    .append(anArr.substring(1)).append(" ")
            }
        }
        return sb.toString().trim { it <= ' ' }
    }

    @JvmStatic
    fun validString(string: String?): Boolean {
        return !string.isNullOrBlank()
    }
}
