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

import com.forcetower.sagres.database.model.SagresDisciplineGroup
import com.forcetower.sagres.utils.ValueUtils
import org.jsoup.nodes.Document

/**
 * Created by João Paulo on 06/03/2018.
 */

object SagresDcpGroupsParser {
    fun getGroups(document: Document): List<SagresDisciplineGroup> {
        val groups = mutableListOf<SagresDisciplineGroup>()

        val disciplines = document.select("section[class=\"webpart-aluno-item\"]")
        for (discipline in disciplines) {
            val semester = discipline.selectFirst("span[class=\"webpart-aluno-periodo\"]").text()
            val title = discipline.selectFirst("a[class=\"webpart-aluno-nome cor-destaque\"]").text()
            val codePos = title.indexOf("-")
            val code = title.substring(0, codePos).trim { it <= ' ' }

            var credits = discipline.select("span[class=\"webpart-aluno-codigo\"]").text()
            credits = credits.replace("[^\\d]".toRegex(), "")


            val ul = discipline.selectFirst("ul")

            if (ul != null) {
                val lis = ul.select("li")
                for (li in lis) {
                    val element = li.selectFirst("a[href]")
                    var type = element.text()
                    val refGroupPos = type.lastIndexOf("(")
                    type = type.substring(0, refGroupPos).trim { it <= ' ' }

                    val group = SagresDisciplineGroup(null, type, 0, 0, null, null, null)
                    group.setDisciplineCodeAndSemester(code, semester)
                    groups.add(group)
                }
            } else {
                val group = SagresDisciplineGroup(null, null, ValueUtils.toInteger(credits), 0, null, null, null)
                group.setDisciplineCodeAndSemester(code, semester)
                groups.add(group)
            }
        }

        return groups
    }
}
