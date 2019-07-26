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
//import com.forcetower.sagres.database.model.SDemandOffer
import okhttp3.FormBody
import okhttp3.RequestBody
import org.jsoup.nodes.Document

import java.util.HashMap

object SagresForms {

    private fun extractHiddenFields(document: Document, formBody: FormBody.Builder) {
        val elements = document.select("input[value][type=\"hidden\"]")
        for (element in elements) {
            val key = element.attr("id")
            val value = element.attr("value")
            formBody.add(key, value)
        }
    }

    fun loginBody(username: String, password: String): RequestBody {
        return FormBody.Builder()
            .add("ctl00\$PageContent\$LoginPanel\$UserName", username)
            .add("ctl00\$PageContent\$LoginPanel\$Password", password)
            .add("ctl00\$PageContent\$LoginPanel\$LoginButton", "Entrar")
            .add("__EVENTTARGET", "")
            .add("__EVENTARGUMENT", "")
            .add("__VIEWSTATE", Constants.getParameter("LOGIN_VIEW_STATE"))
            .add("__VIEWSTATEGENERATOR", Constants.getParameter("LOGIN_VW_STT_GEN"))
            .add("__EVENTVALIDATION", Constants.getParameter("LOGIN_VIEW_VALID"))
            .build()
    }

    fun loginApprovalBody(document: Document): RequestBody {
        val formBody = FormBody.Builder()
        val elements = document.select("input[value][type=\"hidden\"]")
        for (element in elements) {
            val key = element.attr("id")
            val value = element.attr("value")
            formBody.add(key, value)
        }

        formBody.add("ctl00\$btnLogin", "Acessar o SAGRES Portal")
        return formBody.build()
    }

    fun makeFormBodyForDisciplineMaterials(document: Document, encoded: String): FormBody.Builder {
        val builderIn = FormBody.Builder()

        val values = HashMap<String, String>()
        values["ctl00\$MasterPlaceHolder\$RowsPerPage1\$ddMostrar"] = "0"

        val elements = document.select("input[value][type=\"hidden\"]")

        for (elementIn in elements) {
            val id = elementIn.attr("id")
            val `val` = elementIn.attr("value")
            values[id] = `val`
        }

        values["__aspnetForm_ClientStateInput"] = encoded
        values["ctl00\$smpManager"] = "ctl00\$MasterPlaceHolder\$UpdatePanel1|ctl00\$MasterPlaceHolder\$pvMaterialApoio"
        values["_ajax_ctl00_MasterPlaceHolder_dwForm_context"] =
            "(objctl00_MasterPlaceHolder_dwForm 0)(21890 )((currentrow 0)(sortString '?'))"
        values["_ajax_ctl00_MasterPlaceHolder_dwForm_client"] = "(scrollbar 0 0)"
        values["_ajax_ctl00_MasterPlaceHolder_ucPopupConsultaMaterialApoio_dwForm_context"] =
            "(objctl00_MasterPlaceHolder_ucPopupConsultaMaterialApoio_dwForm 0)(22022 )((sortString 'anx_ds_anexo A'))"
        values["_ajax_ctl00_MasterPlaceHolder_ucPopupConsultaMaterialApoio_dwForm_client"] = "(scrollbar 0 0)"
        values["__EVENTTARGET"] = "ctl00\$MasterPlaceHolder\$pvMaterialApoio"
        values["__EVENTARGUMENT"] = "true"
        values["__ctl00_MasterPlaceHolder_pvMaterialApoio_ClientStateInput"] =
            "eyJfcmVhbFR5cGUiOnRydWUsInNob3ckX2luc2VydE5ld1JvdyI6ZmFsc2V9"
        values["__ctl00_MasterPlaceHolder_ucPopupConsultaMaterialApoio_ClientStateInput"] = "eyJfcmVhbFR5cGUiOnRydWV9"
        values["__ctl00_MasterPlaceHolder_ucPopupConsultaPlanoAula_PopupView1_ClientStateInput"] =
            "eyJfcmVhbFR5cGUiOnRydWV9"
        values["ctl00\$HeaderPlaceHolder\$ucCabecalhoClasse\$PainelRetratil1_ClientState"] = "true"
        values["__ASYNCPOST"] = "false"

        for ((key, value1) in values) {
            builderIn.add(key, value1)
        }

        return builderIn
    }

//    fun makeFormBodyForDemand(list: List<SDemandOffer>, document: Document): RequestBody {
//        val form = FormBody.Builder()
//
//        for (offer in list) {
//            form.add(offer.getId(), java.lang.Boolean.toString(offer.getSelected()))
//        }
//
//        val elements = document.select("input[value][type=\"hidden\"]")
//        for (element in elements) {
//            val key = element.attr("id")
//            var value = element.attr("value")
//
//            if (value.trim { it <= ' ' }.isEmpty() && !key.equals(
//                    "__EVENTTARGET",
//                    ignoreCase = true
//                ) && !key.equals("__EVENTARGUMENT", ignoreCase = true) && !key.equals("__VIEWSTATE", ignoreCase = true)
//            ) {
//                value = "eyJfcmVhbFR5cGUiOnRydWV9"
//            }
//
//            if (!key.endsWith("hfChecked")) {
//                form.add(key, value)
//            }
//        }
//
//        form.add("ctl00\$smpManager", "ctl00\$MasterPlaceHolder\$UpdatePanel1|ctl00\$MasterPlaceHolder\$btnSalvar")
//        form.add("__ASYNCPOST", "false")
//        form.add("ctl00\$MasterPlaceHolder\$btnSalvar", "Salvar")
//        return form.build()
//    }

    fun makeFormBodyForAllDisciplines(document: Document): RequestBody {
        val formBody = FormBody.Builder()
        extractHiddenFields(document, formBody)

        formBody.add("ctl00\$MasterPlaceHolder\$ctl00\$ddMostrar", "0")
        formBody.add("ctl00\$MasterPlaceHolder\$FiltroClasses\$imRecuperar", "Exibir")
        formBody.add("ctl00\$MasterPlaceHolder\$FiltroClasses\$ddPeriodosLetivos", "")
        formBody.add("ctl00\$MasterPlaceHolder\$FiltroClasses\$txbFiltroNome", "")
        return formBody.build()
    }

    fun goToDisciplineAlternative(position: String, document: Document): RequestBody {
        val formBody = FormBody.Builder()
        extractHiddenFields(document, formBody)

        formBody.add("__EVENTTARGET", "Selecionar")
        formBody.add("__EVENTARGUMENT", position)
        formBody.add("ctl00\$MasterPlaceHolder\$FiltroClasses\$txbFiltroNome", "")
        formBody.add("ctl00\$MasterPlaceHolder\$ctl00\$ddMostrar", "0")
        return formBody.build()
    }
}
