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

import com.forcetower.sagres.database.model.SagresCourseVariant
import com.forcetower.sagres.database.model.SagresGrade
import com.forcetower.sagres.database.model.SagresGradeInfo
import org.jsoup.nodes.Document

object SagresGradesParser {
    @JvmStatic
    fun extractSemesterCodes(document: Document): List<Pair<Long, String>> {
        val list: MutableList<Pair<Long, String>> = ArrayList()
        val semestersValues = document.selectFirst("select[id=\"ctl00_MasterPlaceHolder_ddPeriodosLetivos_ddPeriodosLetivos\"]")
        if (semestersValues != null) {
            val options = semestersValues.select("option")
            for (option in options) {
                val value = option.attr("value").trim()
                val semester = option.text().trim()
                try {
                    val semesterId = value.toLong()
                    val pair = Pair(semesterId, semester)
                    list.add(pair)
                } catch (e: Exception) {
                }
            }
            return list
        } else {
            return list
        }
    }

    @JvmStatic
    fun getSelectedSemester(document: Document): Pair<Boolean, Long>? {
        val values = document.select("option[selected=\"selected\"]")
        return if (values.size == 1) {
            val value = values[0].attr("value").trim()
            try {
                val id = value.toLong()
                Pair(true, id)
            } catch (e: Exception) {

                null
            }
        } else {
            val defValue = document.selectFirst("select[id=\"ctl00_MasterPlaceHolder_ddPeriodosLetivos_ddPeriodosLetivos\"]")
            if (defValue != null) {
                val selected = defValue.selectFirst("option[selected=\"selected\"]")
                if (selected != null) {
                    val value = selected.attr("value").trim()
                    try {
                        val id = value.toLong()

                        Pair(false, id)
                    } catch (e: Exception) {

                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    @JvmStatic
    fun extractCourseVariants(document: Document): List<SagresCourseVariant> {
        val courses: MutableList<SagresCourseVariant> = ArrayList()
        val variants = document.selectFirst("select[id=\"ctl00_MasterPlaceHolder_ddRegistroCurso\"]")
        if (variants != null) {
            val elements = variants.children()
            for (element in elements) {
                try {
                    val uefsId = element.attr("value").toLong()
                    val name = element.text().trim()
                    courses.add(SagresCourseVariant(uefsId, name))
                } catch (e: Exception) {}
            }
        }

        return courses
    }

    @JvmStatic
    fun canExtractGrades(document: Document): Boolean {
        val bulletin = document.selectFirst("div[id=\"divBoletins\"]")
        return if (bulletin != null) {
            bulletin.selectFirst("div[class=\"boletim-container\"]") != null
        } else {
            false
        }
    }

    @JvmStatic
    fun extractGrades(document: Document, semesterId: Long): List<SagresGrade> {
        val grades: MutableList<SagresGrade> = ArrayList()
        val bulletin = document.selectFirst("div[id=\"divBoletins\"]")
        val classes = bulletin?.select("div[class=\"boletim-container\"]").orEmpty()

        for (clazz in classes) {
            try {
                val info = clazz.selectFirst("div[class=\"boletim-item-info\"]")
                val name = info?.selectFirst("span[class=\"boletim-item-titulo cor-destaque\"]") ?: continue

                val discipline = name.text().trim()
                val grade = SagresGrade(semesterId, discipline)

                val gradeInfo = clazz.selectFirst("div[class=\"boletim-notas\"]") ?: continue
                val table = gradeInfo.selectFirst("table")
                val body = table?.selectFirst("tbody")

                body?.run {
                    var grouping = 1
                    var groupingName = "Notas"
                    val trs = body.select("tr")
                    for (tr in trs) {
                        val children = tr.children()
                        if (children.size == 4) {
                            val td = children[0]
                            if (td.children().size == 0) {
                                val mean = children[2]
                                grade.partialMean = mean.text().trim()
                            } else {
                                val date = children[0].text().trim()
                                val evaluation = children[1].text().trim()
                                val score = children[2].text().trim()
                                var weight = children[3].text().trim().toDoubleOrNull()
                                if (weight == null) weight = 1.0
                                grade.addInfo(SagresGradeInfo(evaluation, score, date, weight, grouping, groupingName))
                            }
                        } else {
                            if (tr.hasClass("boletim-linha-destaque") && children.size == 3) {
                                grouping++
                            } else if (children.size == 2) {
                                val element = children[1]
                                if (element.`is`("th") && element.children().size == 1) {
                                    val child = element.child(0)
                                    if (child.`is`("span")) {
                                        groupingName = child.text()
                                    }
                                }
                            }
                        }
                    }
                    grades.add(grade)
                }

                val foot = table?.selectFirst("tfoot")
                if (foot != null) {
                    val tr = foot.selectFirst("tr")
                    if (tr != null && tr.children().size == 4)
                        grade.finalScore = if (tr.children()[2].text().trim() == "-") "0.0" else tr.children()[2].text().trim()
                }
            } catch (t: Throwable) {

                t.printStackTrace()
            }
        }
        return grades
    }
}
