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

import com.forcetower.sagres.database.model.SagresMessage
import java.util.Locale
import org.jsoup.nodes.Document
import timber.log.Timber
import timber.log.debug

/**
 * Created by João Paulo on 06/03/2018.
 */

object SagresMessageParser {

    @JvmStatic
    fun getMessages(document: Document): List<SagresMessage> {
        val list = mutableListOf<SagresMessage>()

        val articles = document.select("article")
        articles.forEachIndexed { _, article ->
            val scope = article.selectFirst("span[class=\"recado-escopo\"]")?.text()?.trim()
            val dated = article.selectFirst("span[class=\"recado-data\"]")?.text()?.trim()
            val message = article.selectFirst("p[class=\"recado-texto\"]")
                    ?.wholeText()
                    ?.trim()
                    ?.removePrefix("Descrição do Recado:")
                    ?.trim()

            val (attachmentName, attachmentLink) = article.selectFirst("span[class=\"material_apoio_arquivo\"]")
                    ?.run {
                        val link = selectFirst("a[href]")?.attr("href")
                        val name = parent().children().run {
                            if (size >= 3) {
                                get(1).text().trim()
                            }
                            null
                        }
                        name to link
                    } ?: null to null

            if (attachmentLink != null) Timber.debug { "Weow! An attachment $attachmentName $attachmentLink" }
            val info = article.selectFirst("i[class=\"recado-remetente\"]")?.text()
                    ?.trim()
                    ?.removePrefix("De")
                    ?.trim()

            val information = SagresMessage(
                message?.toLowerCase(Locale.getDefault()).hashCode().toLong(),
                null,
                null,
                message,
                -2,
                info,
                null,
                attachmentName,
                attachmentLink
            ).apply {
                isFromHtml = true
                discipline = scope
                dateString = dated
                processingTime = System.currentTimeMillis()
            }
            list.add(information)
        }
        return list
    }
}
