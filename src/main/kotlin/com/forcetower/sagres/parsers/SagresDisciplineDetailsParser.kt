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

import com.forcetower.sagres.database.model.SagresDisciplineClassItem
import com.forcetower.sagres.database.model.SagresDisciplineClassLocation
import com.forcetower.sagres.database.model.SagresDisciplineGroup
import com.forcetower.sagres.utils.ValueUtils.toInteger
import com.forcetower.sagres.utils.WordUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.ArrayList

object SagresDisciplineDetailsParser {

    @JvmStatic
    fun extractDisciplineGroup(document: Document): SagresDisciplineGroup? {
        val elementName = document.selectFirst("h2[class=\"cabecalho-titulo\"]") ?: return null

        val classNameFull = elementName.text()

        val codePos = classNameFull.indexOf("-")
        val code = classNameFull.substring(0, codePos).trim { it <= ' ' }
        val groupPos = classNameFull.lastIndexOf("(")
        val group = classNameFull.substring(groupPos)
        val refGroupPos = group.lastIndexOf("-")
        val refGroup = group.substring(refGroupPos + 1, group.length - 1).trim { it <= ' ' }
        val name = classNameFull.substring(codePos + 1, groupPos).trim { it <= ' ' }

        var teacher = ""
        var elementTeacher: Element? = document.selectFirst("div[class=\"cabecalho-dado nome-capitalizars\"]")
        if (elementTeacher != null) {
            elementTeacher = elementTeacher.selectFirst("span")
            if (elementTeacher != null) teacher = WordUtils.toTitleCase(elementTeacher.text()) ?: ""
        }

        var semesterByName = ""
        var classCredits = ""
        var missLimits = ""
        var classPeriod: String? = null
        var department: String? = null
        val locations = mutableListOf<SagresDisciplineClassLocation>()

        for (element in document.select("div[class=\"cabecalho-dado\"]")) {
            val b = element.child(0)
            val bText = b.text()
            if (bText.equals("Período:", ignoreCase = true)) {
                semesterByName = element.child(1).text()
            } else if (bText.equals("Carga horária:", ignoreCase = true) && classCredits.isEmpty()) {
                classCredits = element.child(1).text()
                classCredits = classCredits.replace("[^\\d]".toRegex(), "").trim()
            } else if (bText.equals("Limite de Faltas:", ignoreCase = true)) {
                missLimits = element.child(1).text()
                missLimits = missLimits.replace("[^\\d]".toRegex(), "").trim()
            } else if (bText.equals("Período de aulas:", ignoreCase = true)) {
                classPeriod = element.selectFirst("span").text()
            } else if (bText.equals("Departamento:", ignoreCase = true)) {
                department = WordUtils.toTitleCase(element.child(1).text())
            } else if (bText.equals("Horário:", ignoreCase = true)) {
                for (classTime in element.select("div[class=\"cabecalho-horario\"]")) {
                    val day = classTime.child(0).text()
                    val start = classTime.child(1).text()
                    val end = classTime.child(3).text()
                    locations.add(SagresDisciplineClassLocation(start, end, day, null, null, null, name, code, group, true))
                }
            }
        }

        if (classCredits.isEmpty()) classCredits = "0"
        if (missLimits.isEmpty()) missLimits = "0"
        var credits = 0
        var maxMiss = 0
        try {
            credits = Integer.parseInt(classCredits)
            maxMiss = Integer.parseInt(missLimits)
        } catch (e: Exception) {
        }

        val created = SagresDisciplineGroup(teacher, refGroup, credits, maxMiss, classPeriod, department, locations)
        created.setDisciplineCodeAndSemester(code, semesterByName)
        created.classItems = extractClassItems(document)
        created.isDraft = false
        created.name = name
        return created
    }

    @JvmStatic
    fun extractClassItems(document: Document): List<SagresDisciplineClassItem> {
        val items = ArrayList<SagresDisciplineClassItem>()

        val trs = document.select("tr[class]")
        for (tr in trs) {
            if (tr.attr("id").contains("header")) continue

            val tds = tr.children()
            if (!tds.isEmpty()) {
                val classItem = getFromTDs(tds)
                if (classItem != null) items.add(classItem)
            }
        }

        return items
    }

    @JvmStatic
    private fun getFromTDs(tds: Elements): SagresDisciplineClassItem? {
        try {
            val strNumber = tds[0].text().trim()
            val situation = tds[1].text()
            val date = tds[2].text()
            var description = tds[3].text()
            val strMaterials = tds[5].text().trim()
            val number = toInteger(strNumber, -1)
            val materials = toInteger(strMaterials, -1)

            if (description.isNullOrBlank()) {
                description = "Não cadastrada"
            }

            if (materials > 0) {
            }

            // Download Material section
            var element = tds[5]
            element = element.selectFirst("a")
            var href = element.attr("HREF")
            if (href.isEmpty()) href = element.attr("href")
            val link = if (href.startsWith("link?")) href.substring(5) else href
            return SagresDisciplineClassItem(number, situation, description, date, materials, link)
        } catch (ignored: Exception) { ignored.printStackTrace() }
        return null
    }
}
