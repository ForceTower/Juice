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

import com.forcetower.sagres.database.model.SagresDiscipline
import com.forcetower.sagres.utils.ValueUtils
import com.forcetower.sagres.utils.WordUtils
import java.util.ArrayList
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object SagresDisciplineParser {
    @JvmStatic
    fun getDisciplines(document: Document): List<SagresDiscipline> {
        val disciplines = ArrayList<SagresDiscipline>()

        val elements = document.select("section[class=\"webpart-aluno-item\"]")
        for (dElement in elements) {
            val title = dElement.selectFirst("a[class=\"webpart-aluno-nome cor-destaque\"]")?.text().orEmpty()
            val period = dElement.selectFirst("span[class=\"webpart-aluno-periodo\"]")?.text().orEmpty()
            var credits = dElement.select("span[class=\"webpart-aluno-codigo\"]").text()
            credits = credits.replace("[^\\d]".toRegex(), "")

            var studentLinks: Element? =
                dElement.selectFirst("div[class=\"webpart-aluno-links webpart-aluno-links-up\"]")
            if (studentLinks == null)
                studentLinks = dElement.selectFirst("div[class=\"webpart-aluno-links webpart-aluno-links-down\"]")
            val misses = studentLinks!!.child(1)
            val missesSpan = misses.selectFirst("span")
            var missedClasses = missesSpan?.text().orEmpty()
            missedClasses = missedClasses.replace("[^\\d]".toRegex(), "")

            var situation: String? = null
            var situationPart: Element? = dElement.selectFirst("div[class=\"webpart-aluno-resultado\"]")
            if (situationPart == null) situationPart =
                dElement.selectFirst("div[class=\"webpart-aluno-resultado estado-sim\"]")
            if (situationPart == null) situationPart =
                dElement.selectFirst("div[class=\"webpart-aluno-resultado estado-nao\"]")
            if (situationPart != null && situationPart.children().size == 2) {
                situation = situationPart.children()[1].text()
                situation = situation!!.lowercase()
                situation = WordUtils.toTitleCase(situation)
                if (situation!!.equals("Não existe resultado final divulgado pelo professor.", ignoreCase = true))
                    situation = "Em aberto"
            }

            var last = ""
            var next = ""
            val lastAndNextClasses = dElement.select("div[class=\"webpart-aluno-detalhe\"]")
            if (lastAndNextClasses.size > 0) {
                val lastSpan = lastAndNextClasses[0].selectFirst("span")
                last = lastSpan?.text().orEmpty()
            }

            if (lastAndNextClasses.size > 1) {
                val nextSpan = lastAndNextClasses[1].selectFirst("span")
                next = nextSpan?.text().orEmpty()
            }

            val codePos = title.indexOf("-")
            val code = title.substring(0, codePos).trim()
            val name = title.substring(codePos + 1).trim()

            val discipline = SagresDiscipline(period, name, code)
            discipline.credits = ValueUtils.toInteger(credits)
            discipline.missedClasses = ValueUtils.toInteger(missedClasses)
            discipline.lastClass = last
            discipline.nextClass = next
            discipline.situation = situation
            disciplines.add(discipline)
        }

        return disciplines
    }
}
