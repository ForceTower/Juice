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

import com.google.gson.annotations.SerializedName

class SagresDisciplineResumed(
    var id: Long,
    @SerializedName("codigo")
    var code: String?,
    @SerializedName("nome")
    var name: String?,
    @SerializedName("nomeResumido")
    var resumedName: String?,
    @SerializedName("ementa")
    var objective: String?,
    var departmentLink: String?,
    var link: String?
) {
    @SerializedName("departamento")
    var department: SagresLinker? = null
}
