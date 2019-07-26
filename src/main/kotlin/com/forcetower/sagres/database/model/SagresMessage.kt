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

class SagresMessage(
    var sagresId: Long,
    var timestamp: String?,
    var sender: SagresLinker?,
    var message: String?,
    var senderProfile: Int,
    var senderName: String?,
    var scopes: SagresLinker?,
    var attachmentName: String?,
    var attachmentLink: String?
) : Comparable<SagresMessage>, Timestamped {
    var discipline: String? = null

    var disciplineCode: String? = null
    var objective: String? = null

    var isFromHtml: Boolean = false
    var dateString: String? = null
    var processingTime: Long = 0

    val timeStampInMillis: Long
        get() {
            try {
                return getInMillis(timestamp)!!
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return -1
        }

    init {
        this.isFromHtml = false
        this.processingTime = System.currentTimeMillis()
    }

    override fun compareTo(other: SagresMessage): Int {
        return timeStampInMillis.compareTo(other.timeStampInMillis)
    }

    override fun toString(): String {
        val name = senderName
        return (name ?: "null") + "\n-> " + message + "\n\n"
    }
}
