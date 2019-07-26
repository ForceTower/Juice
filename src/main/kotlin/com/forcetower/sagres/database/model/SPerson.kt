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

package com.forcetower.sagres.database.model

class SPerson(
    var id: Long,
    name: String,
    var exhibitionName: String?,
    private var cpf: String?,
    var email: String?
) {
    var name: String? = null
        get() {
            this.name = field!!.trim { it <= ' ' }
            return field
        }
    var sagresId: String? = null
    var isMocked: Boolean = false

    val unique: String
        get() = cpf!!.toLowerCase() + ".." + id

    init {
        this.name = name
        this.isMocked = false
    }

    fun getCpf(): String? {
        if (cpf == null) return null

        cpf = cpf!!.trim { it <= ' ' }
        return cpf
    }

    fun setCpf(cpf: String) {
        this.cpf = cpf
    }

    override fun toString(): String {
        return "ID: $id - Name: $name"
    }
}
