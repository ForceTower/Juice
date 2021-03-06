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

package com.forcetower.sagres.operation.disciplines

import com.forcetower.sagres.database.model.SagresDiscipline
import com.forcetower.sagres.database.model.SagresDisciplineGroup
import com.forcetower.sagres.operation.BaseCallback
import com.forcetower.sagres.operation.Status

class FastDisciplinesCallback(status: Status) : BaseCallback<FastDisciplinesCallback>(status) {
    private var groups: List<SagresDisciplineGroup> = emptyList()
    private var flags: Int = 0
    private var current: Int = 0
    private var total: Int = 0
    private var failureCount: Int = 0
    private var semesters: List<Pair<Long, String>> = emptyList()

    fun getFlags() = flags
    fun getGroups() = groups
    fun getCurrent() = current
    fun getTotal() = total
    fun getFailureCount() = failureCount
    fun getSemesters() = semesters

    fun getDisciplines(): List<SagresDiscipline> {
        return groups.groupBy { it.code }.map { entry ->
            val value = entry.value
            val code = entry.key

            val creditsSum = value.groupBy { it.semester }.map { it.value.distinctBy { clazz -> clazz.group }.sumOf { group -> group.credits } }
                .maxOrNull() ?: 0
            val first = value.first()
            SagresDiscipline(first.semester, first.name!!, code).apply {
                credits = creditsSum
            }
        }
    }

    fun groups(groups: List<SagresDisciplineGroup>): FastDisciplinesCallback {
        this.groups = groups
        return this
    }

    fun flags(flags: Int = 0): FastDisciplinesCallback {
        this.flags = flags
        return this
    }

    fun current(current: Int): FastDisciplinesCallback {
        this.current = current
        return this
    }

    fun total(total: Int): FastDisciplinesCallback {
        this.total = total
        return this
    }

    fun failureCount(failureCount: Int): FastDisciplinesCallback {
        this.failureCount = failureCount
        return this
    }

    fun semesters(semesters: List<Pair<Long, String>>): FastDisciplinesCallback {
        this.semesters = semesters
        return this
    }

    companion object {
        fun copyFrom(callback: BaseCallback<*>): FastDisciplinesCallback {
            return FastDisciplinesCallback(callback.status).message(callback.message).code(callback.code).throwable(
                callback.throwable
            ).document(callback.document)
        }

        const val LOGIN = 1
        const val INITIAL = 2
        const val PROCESSING = 4
        const val DOWNLOADING = 8
        const val SAVING = 16
        const val GRADES = 32
    }
}
