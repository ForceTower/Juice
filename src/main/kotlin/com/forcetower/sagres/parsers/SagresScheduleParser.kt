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

package com.forcetower.sagres.parsers

import com.forcetower.sagres.database.model.SDisciplineClassLocation
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber

import java.util.ArrayList
import java.util.HashMap

import com.forcetower.sagres.utils.DateUtils.getDayOfWeek
import timber.log.debug

/**
 * Created by João Paulo on 07/03/2018.
 */

object SagresScheduleParser {
    private lateinit var iterationPerDay: HashMap<Int, String>
    private lateinit var codePerLessons: HashMap<String, SagresClass>

    @Synchronized
    fun getSchedule(document: Document): List<SDisciplineClassLocation>? {
        val schedule = document.selectFirst("table[class=\"meus-horarios\"]")
        val subtitle = document.selectFirst("table[class=\"meus-horarios-legenda\"]")

        if (schedule == null || subtitle == null) {
            Timber.debug { "Schedule not found! Prob is \"Schedule Undefined\"" }
            return null
        }

        iterationPerDay = HashMap()
        codePerLessons = HashMap()

        findSchedule(schedule)
        findDetails(subtitle)
        val classDay = getSchedule(codePerLessons)
        return convertToNewType(classDay)
    }

    @Synchronized
    private fun convertToNewType(hashMap: HashMap<String, List<SagresClassDay>>): List<SDisciplineClassLocation> {
        val disciplineClassLocations = ArrayList<SDisciplineClassLocation>()
        for (key in hashMap.keys) {
            val classDays = hashMap[key] ?: continue

            for (classDay in classDays) {
                val location = SDisciplineClassLocation(
                    classDay.starts_at,
                    classDay.ends_at,
                    classDay.day,
                    classDay.room,
                    classDay.campus,
                    classDay.modulo,
                    classDay.class_name,
                    classDay.class_code,
                    classDay.classType,
                    false
                )
                disciplineClassLocations.add(location)
            }
        }

        return disciplineClassLocations
    }

    @Synchronized
    private fun findSchedule(schedule: Element) {
        val trs = schedule.select("tr")

        for (i in trs.indices) {
            val tr = trs[i]
            if (i == 0) {
                //Header -> days of class
                val ths = tr.select("th")
                for (j in ths.indices) {
                    val th = ths[j]
                    if (th.text().isNotBlank()) {
                        iterationPerDay[j] = th.text().trim { it <= ' ' }
                    }
                }
            } else {
                val tds = tr.select("td")
                var start = ""
                var end = ""
                for (j in tds.indices) {
                    val td = tds[j]

                    val classTime = td.text().trim { it <= ' ' }
                    if (classTime.trim { it <= ' ' }.isEmpty()) {
                        continue
                    }

                    val parts = classTime.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val one = parts[0].trim { it <= ' ' }
                    val two = parts[1].trim { it <= ' ' }

                    if (j == 0) {
                        start = one
                        end = two
                    } else {
                        val day = iterationPerDay[j] ?: continue
                        var clazz: SagresClass? = codePerLessons[one]

                        if (clazz == null) clazz = SagresClass(one)

                        clazz.addClazz(two)
                        clazz.addStartEndTime(start, end, day, two)

                        codePerLessons[one] = clazz
                    }
                }
            }
        }
    }

    @Synchronized
    private fun findDetails(subtitle: Element) {
        val trs = subtitle.select("tr")

        var currentCode = "undef"
        for (i in trs.indices) {
            val tr = trs[i]
            val tds = tr.select("td")
            val value = tds[1].text()

            val td = tds[0]
            if (td.html().contains("&nbsp;")) {
                val parts = value.split("::")
                if (parts.size == 2) {
                    if (currentCode != "undef") {
                        val lesson = codePerLessons[currentCode]
                        if (lesson != null)
                            lesson.addAtToAllClasses(parts[0].trim(), parts[1].trim())
                        else {
                            Timber.debug { "Something wrong is happening here..." }
                        }
                    }
                } else if (parts.size == 3) {
                    if (currentCode != "undef") {
                        val lesson = codePerLessons[currentCode]
                        if (lesson != null)
                            lesson.addAtToSpecificClass(
                                parts[2].trim { it <= ' ' },
                                parts[1].trim { it <= ' ' },
                                parts[0].trim { it <= ' ' })
                        else {
                            Timber.debug { "Something wrong is happening here..." }
                        }
                    }
                } else {
                    Timber.debug { "Something smells fishy" }
                }
            } else {
                val splitPos = value.indexOf("-")
                val code = value.substring(0, splitPos).trim()
                val name = value.substring(splitPos + 1).trim()

                currentCode = code
                val lesson = codePerLessons[code]
                if (lesson != null) {
                    lesson.name = name
                    codePerLessons[code] = lesson
                } else {
                    Timber.debug { "Something was ignored due to a bug. Since this might be changed leave as is" }
                }
            }
        }
    }

    @Synchronized
    private fun getSchedule(classes: HashMap<String, SagresClass>): HashMap<String, List<SagresClassDay>> {
        val classPerDay = HashMap<String, List<SagresClassDay>>()

        if (classes.isEmpty())
            return classPerDay

        for (i in 1..7) {
            val dayOfWeek = getDayOfWeek(i)
            val dayOfClass = ArrayList<SagresClassDay>()

            for (uClass in classes.values) {
                val days = uClass.getDays()

                for (clazz in days) {
                    if (clazz.day.equals(dayOfWeek, ignoreCase = true)) {
                        dayOfClass.add(clazz)
                    }
                }
            }

            if (dayOfClass.isNotEmpty()) classPerDay[dayOfWeek] = dayOfClass
        }

        return classPerDay
    }

    private class SagresClass internal constructor(@get:Synchronized val code: String) {
        @get:Synchronized
        var name: String? = null
            @Synchronized set(name) {
                field = name
                name ?: return
                for (classDay in days) {
                    classDay.setClassName(name)
                }
            }
        private val classes: MutableList<String>
        private val days: MutableList<SagresClassDay>

        init {
            classes = ArrayList()
            days = ArrayList()
        }

        internal fun addClazz(aClassT: String) {
            var aClass = aClassT
            aClass = aClass.trim()
            if (!containsClazz(aClass))
                this.classes.add(aClass)
        }

        @Synchronized
        internal fun addStartEndTime(start: String, finish: String, day: String, classType: String) {
            val classDay = SagresClassDay(start, finish, day, classType, this)
            days.add(classDay)
        }

        @Synchronized
        private fun containsClazz(clazz: String): Boolean {
            return classes.contains(clazz)
        }

        @Synchronized
        internal fun addAtToAllClasses(type: String, at: String) {
            val parts = at.split(",").map { it.trim() }

            val allocatedRoom: String
            val place: String
            val campus: String

            when {
                parts.size == 3 -> {
                    campus = parts[0]
                    place = parts[1]
                    allocatedRoom = removeRoomName(parts[2])
                }
                parts.size == 2 -> {
                    campus = parts[0]
                    allocatedRoom = removeRoomName(parts[1])
                    place = ""
                }
                else -> {
                    allocatedRoom = removeRoomName(parts[0])
                    campus = ""
                    place = ""
                }
            }

            for (classDay in days) {
                if (classDay.classType.equals(type, ignoreCase = true)) {
                    classDay.room = allocatedRoom
                    classDay.setCampus(campus)
                    classDay.setModulo(place)
                }
            }
        }

        @Synchronized
        internal fun addAtToSpecificClass(at: String, day: String, type: String) {
            val parts = at.split(",").map { it.trim() }

            val allocatedRoom: String
            val place: String
            val campus: String

            when {
                parts.size == 3 -> {
                    campus = parts[0]
                    place = parts[1]
                    allocatedRoom = removeRoomName(parts[2])
                }
                parts.size == 2 -> {
                    campus = parts[0]
                    allocatedRoom = removeRoomName(parts[1])
                    place = ""
                }
                else -> {
                    allocatedRoom = removeRoomName(parts[0])
                    campus = ""
                    place = ""
                }
            }

            for (classDay in days) {
                if (classDay.day == day && classDay.classType == type) {
                    classDay.room = allocatedRoom
                    classDay.setCampus(campus)
                    classDay.setModulo(place)
                }
            }
        }

        @Synchronized
        private fun removeRoomName(parted: String): String {
            var part = parted
            if (part.startsWith("Sala")) {
                part = part.substring(4)
            }

            return part.trim { it <= ' ' }
        }

        internal fun getDays(): List<SagresClassDay> {
            return days
        }
    }

    private class SagresClassDay internal constructor(
        val class_code: String,
        var class_name: String?,
        val starts_at: String,
        val ends_at: String,
        val classType: String,
        val day: String,
        var room: String?,
        var campus: String?,
        var modulo: String?
    ) {

        @Synchronized
        internal fun setCampus(campus: String) {
            this.campus = campus
        }

        @Synchronized
        internal fun setModulo(modulo: String) {
            this.modulo = modulo
        }

        @Synchronized
        internal fun setClassName(class_name: String) {
            this.class_name = class_name
        }

        internal constructor(
            start: String,
            finish: String,
            day: String,
            classType: String,
            sagresClass: SagresClass
        ) : this(sagresClass.code, sagresClass.name, start, finish, classType, day, null, null, null) {
        }
    }
}
