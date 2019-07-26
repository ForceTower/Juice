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

package com.forcetower.sagres.operation.initial

import com.forcetower.sagres.database.model.*
import com.forcetower.sagres.operation.BaseCallback
import com.forcetower.sagres.operation.Status

class StartPageCallback(status: Status) : BaseCallback<StartPageCallback>(status) {
    var calendar: List<SagresCalendar>? = null
        private set
    var semesters: List<SagresSemester>? = null
        private set
    var disciplines: List<SagresDiscipline>? = null
        private set
    var groups: List<SagresDisciplineGroup>? = null
        private set
    var messages: List<SagresMessage>? = null
        private set
    var locations: List<SagresDisciplineClassLocation>? = null
        private set
    var isDemandOpen = false
        private set

    fun calendar(calendar: List<SagresCalendar>?): StartPageCallback {
        this.calendar = calendar
        return this
    }

    fun semesters(semesters: List<SagresSemester>): StartPageCallback {
        this.semesters = semesters
        return this
    }

    fun disciplines(disciplines: List<SagresDiscipline>): StartPageCallback {
        this.disciplines = disciplines
        return this
    }

    fun groups(groups: List<SagresDisciplineGroup>): StartPageCallback {
        this.groups = groups
        return this
    }

    fun locations(locations: List<SagresDisciplineClassLocation>?): StartPageCallback {
        this.locations = locations
        return this
    }

    fun demandOpen(demandOpen: Boolean): StartPageCallback {
        this.isDemandOpen = demandOpen
        return this
    }

    fun messages(messages: List<SagresMessage>): StartPageCallback {
        this.messages = messages
        return this
    }
}
