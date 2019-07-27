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

package com.forcetower.sagres.request

import com.forcetower.sagres.Constants
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.RequestBody
import org.jsoup.nodes.Document

object SagresRequests {
    private const val BASE_URL = "http://academico2.uefs.br/Api/SagresApi"

    val currentGrades: Request
        get() = Request.Builder()
            .url(Constants.getUrl("SAGRES_GRADE_PAGE"))
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()

    val demandPage: Request
        get() = Request.Builder()
            .url(Constants.getUrl("SAGRES_DEMAND_OFFERS"))
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()

    val requestedServices: Request
        get() = Request.Builder()
            .url(Constants.getUrl("SAGRES_REQUESTED_SERVICES"))
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()

    val messagesPage: Request
        get() = Request.Builder()
            .url(Constants.getUrl("SAGRES_MESSAGES_PAGE"))
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()

    val allDisciplinesPage: Request
        get() = Request.Builder()
            .url(Constants.getUrl("SAGRES_ALL_DISCIPLINES_PAGE"))
            .tag("disciplines")
            .get()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()

    fun loginRequest(body: RequestBody): Request {
        return Request.Builder()
            .url(Constants.getUrl("SAGRES_LOGIN_PAGE"))
            .tag("aLogin")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun loginApprovalRequest(url: String, body: RequestBody): Request {
        return Request.Builder()
            .url("http://$url")
            .post(body)
            .tag("aLogin")
            .addHeader("x-requested-with", "XMLHttpRequest")
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun me(): Request {
        return Request.Builder()
            .url("$BASE_URL/eu")
            .build()
    }

    //    public static Request link(SLinker linker) {
    //        String link = linker.getLink();
    //        String url = BASE_URL + (link.startsWith("/") ? link : "/" + link);
    //        return new Request.Builder().url(url).build();
    //    }

    fun link(href: String): Request {
        val url = BASE_URL + if (href.startsWith("/")) href else "/$href"
        return Request.Builder().url(url).build()
    }

    fun getPerson(userId: Long): Request {
        val url = "$BASE_URL/registro/pessoas/$userId"
        return Request.Builder().url(url).build()
    }

    fun messages(userId: Long): Request {
        val url = "$BASE_URL/diario/recados?idPessoa=$userId"
        return Request.Builder()
            .url(url)
            .build()
    }

    fun startPage(): Request {
        return Request.Builder()
            .url(Constants.getUrl("SAGRES_DIARY_PAGE"))
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun getSemesters(userId: Long): Request {
        val url = "$BASE_URL/diario/periodos-letivos?idPessoa=$userId&perfil=1"
        return Request.Builder()
            .url(url)
            .build()
    }

    fun getGradesForSemester(semester: Long, document: Document, variant: Long?): Request {
        val formBody = FormBody.Builder()

        val elements = document.select("input[value][type=\"hidden\"]")

        for (element in elements) {
            val key = element.attr("id")
            val value = element.attr("value")
            formBody.add(key, value)
        }

        formBody.add(
            "ctl00\$MasterPlaceHolder\$ddPeriodosLetivos\$ddPeriodosLetivos",
            java.lang.Long.valueOf(semester).toString()
        )
        if (variant != null) {
            formBody.add("ctl00\$MasterPlaceHolder\$ddRegistroCurso", variant.toString())
        }
        formBody.add("ctl00\$MasterPlaceHolder\$imRecuperar", "Exibir")
        return Request.Builder()
            .url(Constants.getUrl("SAGRES_GRADE_ANY"))
            .post(formBody.build())
            .addHeader("x-requested-with", "XMLHttpRequest")
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun getPageRequest(url: String): Request {
        return Request.Builder()
            .addHeader("Accept", "*/*")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.92 Safari/537.36"
            )
            .url(url)
            .build()
    }

    fun postAtStudentPage(builder: FormBody.Builder): Request {
        return Request.Builder()
            .url(Constants.getUrl("SAGRES_DIARY_PAGE"))
            .post(builder.build())
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun getDisciplinePageWithParams(params: FormBody.Builder): Request {
        return Request.Builder()
            .url(Constants.getUrl("SAGRES_CLASS_PAGE"))
            .post(params.build())
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun createDemandWithParams(body: RequestBody): Request {
        return Request.Builder()
            .url(Constants.getUrl("SAGRES_DEMAND_OFFERS"))
            .post(body)
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .build()
    }

    fun postAllDisciplinesParams(body: RequestBody): Request {
        return Request.Builder()
            .url(Constants.getUrl("SAGRES_ALL_DISCIPLINES_PAGE"))
            .tag("disciplines")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }
}
