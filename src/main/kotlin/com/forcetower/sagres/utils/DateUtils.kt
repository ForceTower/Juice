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

object DateUtils {
    @JvmStatic
    fun getDayOfWeek(text: String): Int {
        return when {
            text.equals("SEG", ignoreCase = true) -> 1
            text.equals("TER", ignoreCase = true) -> 2
            text.equals("QUA", ignoreCase = true) -> 3
            text.equals("QUI", ignoreCase = true) -> 4
            text.equals("SEX", ignoreCase = true) -> 5
            text.equals("SAB", ignoreCase = true) -> 6
            text.equals("DOM", ignoreCase = true) -> 0
            else -> 99
        }
    }

    @JvmStatic
    fun getDayOfWeek(i: Int): String {
        return when (i) {
            1 -> "SEG"
            2 -> "TER"
            3 -> "QUA"
            4 -> "QUI"
            5 -> "SEX"
            6 -> "SAB"
            7 -> "DOM"
            else -> "???"
        }
    }
}
