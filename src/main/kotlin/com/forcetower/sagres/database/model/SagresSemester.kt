/*
 * This file is part of the UNES Open Source Project.
 * UNES is licensed under the GNU GPLv3.
 *
 * Copyright (c) 2019.  João Paulo Sena <joaopaulo761@gmail.com>
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

package com.forcetower.sagres.database.model

import com.forcetower.sagres.database.Timestamped

data class SagresSemester(
    var uefsId: Long,
    var name: String,
    var codename: String,
    var start: String?,
    var end: String?,
    var startClasses: String?,
    var endClasses: String?
) : Comparable<SagresSemester>, Timestamped {

    val startInMillis: Long?
        get() = getInMillis(start, -1)

    val endInMillis: Long?
        get() = getInMillis(end, -1)

    val startClassesInMillis: Long?
        get() = getInMillis(startClasses, -1)

    val endClassesInMillis: Long?
        get() = getInMillis(endClasses, -1)

    override fun compareTo(other: SagresSemester): Int {
        return try {
            val o1 = name
            val o2 = other.name
            val str1 = Integer.parseInt(o1.substring(0, 5))
            val str2 = Integer.parseInt(o2.substring(0, 5))

            if (str1 == str2) {
                if (o1.length > 5) -1 else 1
            } else {
                str1.compareTo(str2) * -1
            }
        } catch (e: Exception) {
            0
        }

    }

    companion object {
        fun getCurrentSemester(semesters: List<SagresSemester>?): SagresSemester {
            if (semesters == null || semesters.isEmpty()) {
                return SagresSemester(0, "2018.2", "20182", "", "", "", "")
            }
            return semesters.sorted()[0]
        }
    }
}
