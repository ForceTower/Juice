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

import com.forcetower.sagres.utils.WordUtils.validString

class SagresDisciplineGroup(
    var teacher: String?,
    var group: String?,
    var credits: Int,
    var missLimit: Int,
    var classPeriod: String?,
    var department: String?,
    val locations: List<SagresDisciplineClassLocation>?
) {
    var isDraft = true
    var ignored = 0
    lateinit var semester: String
        private set
    lateinit var code: String
    lateinit var name: String
    lateinit var classItems: List<SagresDisciplineClassItem>

    fun setDisciplineCodeAndSemester(code: String, semester: String) {
        this.code = code
        this.semester = semester
    }

    fun selectiveCopy(other: SagresDisciplineGroup?) {
        if (other == null) return

        if (validString(other.teacher) || teacher == null) teacher = other.teacher
        if (validString(other.group) || group == null) group = other.group
        if (validString(other.classPeriod) || classPeriod == null) classPeriod = other.classPeriod
        if (validString(other.department) || department == null) department = other.department
        if (other.credits > 0 || credits == 0) credits = other.credits
        if (other.missLimit > 0 || missLimit == 0) missLimit = other.missLimit
    }

    override fun toString(): String {
        return "$code:$group::$name"
    }
}
