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

import com.forcetower.sagres.database.model.SagresDisciplineMissedClass
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object SagresMissedClassesParser {

    @JvmStatic
    fun extractMissedClasses(document: Document, semesterId: Long): Pair<Boolean, List<SagresDisciplineMissedClass>> {
        var error = false
        val values = mutableListOf<SagresDisciplineMissedClass>()

        try {
            val div = document.selectFirst("div[id=\"divBoletins\"]")
            val classes = div?.select("div[class=\"boletim-container\"]").orEmpty()

            for (clazz in classes) {
                val info = clazz.selectFirst("div[class=\"boletim-item-info\"]")
                val name = info?.selectFirst("span[class=\"boletim-item-titulo cor-destaque\"]") ?: continue

                val text = name.text()
                val code = text.substring(0, text.indexOf("-") - 1).trim()

                val frequency = clazz.selectFirst("div[class=\"boletim-frequencia\"]")
                val spectrum = frequency?.selectFirst("table")
                if (spectrum != null) {
                    val result = mutableListOf<SagresDisciplineMissedClass>()
                    try {
                        val points = spectrum.children().drop(1)
                        // New heuristic
                        for (i in points.indices step 2) {
                            val head = points[i]
                            val body = points[i + 1]
                            val groupSpan = head.selectFirst("span")
                            val group = groupSpan?.text().orEmpty().split("-")[0].trim()
                            result.addAll(fourier(body, code, semesterId, group, true))
                        }
                    } catch (error: Throwable) {
                        // fallback to original behaviour
                        result.clear()
                        val bodies = spectrum.select("tbody")
                        bodies.forEach { body ->
                            result.addAll(fourier(body, code, semesterId, "inv"))
                        }
                    } finally {
                        values.addAll(result)
                    }
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            error = true
        }

        return Pair(error, values)
    }

    @JvmStatic
    private fun fourier(element: Element, code: String, semesterId: Long, group: String, throwError: Boolean = false): List<SagresDisciplineMissedClass> {
        val values: MutableList<SagresDisciplineMissedClass> = ArrayList()
        val indexes = element.select("tr")

        for (index in indexes) {
            try {
                val information = index.child(0).child(0).children()
                val date = information[0].text().trim()
                val desc = information[1].text().trim()
                values.add(SagresDisciplineMissedClass(date, desc, code, semesterId, group))
            } catch (error: Throwable) {
                if (throwError) throw error
            }
        }
        return values
    }
}
