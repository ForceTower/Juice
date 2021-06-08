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
    fun currentGrades(institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_GRADE_PAGE"))
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun demandPage(institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_DEMAND_OFFERS"))
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun requestedServices(institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_REQUESTED_SERVICES"))
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun messagesPage(institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_MESSAGES_PAGE"))
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun allDisciplinesPage(institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_ALL_DISCIPLINES_PAGE"))
            .tag("disciplines")
            .get()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun loginRequest(body: RequestBody, institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_LOGIN_PAGE"))
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

    fun startPage(institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_DIARY_PAGE"))
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun getGradesForSemester(semester: Long, document: Document, variant: Long?, institution: String): Request {
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
            .url(Constants.forInstitution(institution).getUrl("SAGRES_GRADE_ANY"))
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

    fun postAtStudentPage(builder: FormBody.Builder, institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_DIARY_PAGE"))
            .post(builder.build())
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun getDisciplinePageWithParams(params: FormBody.Builder, institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_CLASS_PAGE"))
            .post(params.build())
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun createDemandWithParams(body: RequestBody, institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_DEMAND_OFFERS"))
            .post(body)
            .addHeader("content-type", "application/x-www-form-urlencoded")
            .build()
    }

    fun postAllDisciplinesParams(body: RequestBody, institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_ALL_DISCIPLINES_PAGE"))
            .tag("disciplines")
            .post(body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("cache-control", "no-cache")
            .build()
    }

    fun ohMyZsh(institution: String): Request {
        return Request.Builder()
            .url(Constants.forInstitution(institution).getUrl("SAGRES_SHITTY_URL"))
            .tag("shitty_url")
            .build()
    }
}
