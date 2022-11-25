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

package com.forcetower.sagres

import java.util.Locale
import kotlin.IllegalArgumentException

class Constants(private val institution: String) {
    companion object {
        /**
         * The Sagres Server Constants
         * This provides a map of all attributes needed for UNES to connect to SAGRES.
         *
         * BASE_URL: The base address of the site
         * LOGIN_VIEW_STATE: A random generated string encoded in base64. You can get your one by inspecting the SAGRES login page it is the __VIEWSTATE variable
         * LOGIN_VW_STT_GEN: Another generated string. You can find it by the __VIEWSTATEGENERATOR variable
         * LOGIN_VIEW_VALID: Random generated string. It's variable name on HTML is __EVENTVALIDATION
         */
        private val SAGRES_SERVER_CONSTANTS = mapOf(
            "UEFS" to mapOf(
                "BASE_URL" to "http://academico.uefs.br/PortalSagres",
                "LOGIN_VIEW_STATE" to "/wEPDwULLTE1ODU0NDkxODMPZBYCZg9kFgQCAQ9kFhACBA8WAh4EaHJlZgU9fi9BcHBfVGhlbWVzL05ld1RoZW1lL0FjZXNzb0V4dGVybm8uY3NzP2ZwPTYzNzAyNzc1MTk0MDAwMDAwMGQCBQ8WAh8ABTx+L0FwcF9UaGVtZXMvTmV3VGhlbWUvY2FwdGNoYS1oYWNrLmNzcz9mcD02MzcyMTMzNTQzOTYzNDg4NDdkAgYPFgIfAAU4fi9BcHBfVGhlbWVzL05ld1RoZW1lL0NvbnRldWRvLmNzcz9mcD02MzcwMjc3NTE5NDAwMDAwMDBkAgcPFgIfAAU5fi9BcHBfVGhlbWVzL05ld1RoZW1lL0VzdHJ1dHVyYS5jc3M/ZnA9NjM3MDI3NzUxOTQwMDAwMDAwZAIIDxYCHwAFOX4vQXBwX1RoZW1lcy9OZXdUaGVtZS9NZW5zYWdlbnMuY3NzP2ZwPTYzNzAyNzc1MTkwMDAwMDAwMGQCCQ8WAh8ABTZ+L0FwcF9UaGVtZXMvTmV3VGhlbWUvUG9wVXBzLmNzcz9mcD02MzcwMjc3NTE5MDAwMDAwMDBkAgoPFgIfAAU7fi9BcHBfVGhlbWVzL05ld1RoZW1lL3NvY2lhbC1oYWNrLmNzcz9mcD02MzcyMDI5ODg3NjU2OTg5NTdkAgsPFgIfAAVYL1BvcnRhbC9SZXNvdXJjZXMvU3R5bGVzL0FwcF9UaGVtZXMvTmV3VGhlbWUvTmV3VGhlbWUwMS9lc3RpbG8uY3NzP2ZwPTYzNjEwNTgyNjY0MDAwMDAwMGQCAw9kFgQCBw8PFgQeBFRleHQFDVNhZ3JlcyBQb3J0YWweB1Zpc2libGVoZGQCCw9kFgYCAQ8PFgIfAmhkZAIDDzwrAAoBAA8WAh4NUmVtZW1iZXJNZVNldGhkZAIFD2QWAgICD2QWAgIBDxYCHgtfIUl0ZW1Db3VudGZkZEQ9dOxjZzHs804b7zNS9kZWDDqS",
                "LOGIN_VW_STT_GEN" to "BB137B96",
                "LOGIN_VIEW_VALID" to "/wEdAAQ311wKHGQUWoLwp7PBHoaoM4nqN81slLG8uEFL8sVLUjoauXZ8QTl2nEJmPx53FYhjUq3W1Gjeb7bKHHg4dlobTvKU+hsWpu5LuPm5L8dsMwBIjEY=",
                "REQUIRES_CAPTCHA" to "true",
                "CAPTCHA_SITE_KEY" to "6Lc5M-UUAAAAAOFIqIdUEP2BeaqFi3f-71HscRlB",
                "CAPTCHA_BASE" to "http://academico.uefs.br"
            ),
            "UESC" to mapOf(
                "BASE_URL" to "http://www.prograd.uesc.br/PortalSagres",
                "LOGIN_VIEW_STATE" to "/wEPDwULLTE1ODU0NDkxODMPZBYCZg9kFgQCAQ9kFhICBA8WAh4EaHJlZgU9fi9BcHBfVGhlbWVzL05ld1RoZW1lL0FjZXNzb0V4dGVybm8uY3NzP2ZwPTYzNzMxNjI2NTgyMDAwMDAwMGQCBQ8WAh8ABTx+L0FwcF9UaGVtZXMvTmV3VGhlbWUvY2FwdGNoYS1oYWNrLmNzcz9mcD02MzcyMTMzNTQzOTYzNDg4NDdkAgYPFgIfAAU4fi9BcHBfVGhlbWVzL05ld1RoZW1lL0NvbnRldWRvLmNzcz9mcD02MzczMTYyNjU4MjAwMDAwMDBkAgcPFgIfAAU5fi9BcHBfVGhlbWVzL05ld1RoZW1lL0VzdHJ1dHVyYS5jc3M/ZnA9NjM3MzE2MjY1ODIwMDAwMDAwZAIIDxYCHwAFOX4vQXBwX1RoZW1lcy9OZXdUaGVtZS9NZW5zYWdlbnMuY3NzP2ZwPTYzNzMxNjI2NTgyMDAwMDAwMGQCCQ8WAh8ABTZ+L0FwcF9UaGVtZXMvTmV3VGhlbWUvUG9wVXBzLmNzcz9mcD02MzczMTYyNjU4MjAwMDAwMDBkAgoPFgIfAAU7fi9BcHBfVGhlbWVzL05ld1RoZW1lL3NvY2lhbC1oYWNrLmNzcz9mcD02MzcyMDg0NDA3ODk4NTI5MDRkAgsPFgIfAAVeL1BvcnRhbFNhZ3Jlcy9SZXNvdXJjZXMvU3R5bGVzL0FwcF9UaGVtZXMvTmV3VGhlbWUvTmV3VGhlbWUwMS9lc3RpbG8uY3NzP2ZwPTYzNjEwNTgyNjY0MDAwMDAwMGQCDQ8WAh8ABUEvUG9ydGFsU2FncmVzL1Jlc291cmNlcy9TdHlsZXMvYXRvb2x0aXAuY3NzP2ZwPTYzNjg4MDY4MDE4MDAwMDAwMGQCAw9kFgQCBw8PFgQeBFRleHQFDVNhZ3JlcyBQb3J0YWweB1Zpc2libGVoZGQCCw9kFgYCAQ8PFgIfAmhkZAIDDzwrAAoBAA8WAh4NUmVtZW1iZXJNZVNldGhkZAIFD2QWAgICD2QWAgIBDxYCHgtfIUl0ZW1Db3VudGZkZNxlCM74gLWCyXopc6ixMkAv4k+N",
                "LOGIN_VW_STT_GEN" to "065EA922",
                "LOGIN_VIEW_VALID" to "/wEdAATgRfgRLTq4lSGbh8lPUxxfM4nqN81slLG8uEFL8sVLUjoauXZ8QTl2nEJmPx53FYhjUq3W1Gjeb7bKHHg4dlobmeL7Jl6YZrIyT+BYJFKnIAKvBSU=",
                "REQUIRES_CAPTCHA" to "true",
                "CAPTCHA_SITE_KEY" to "6LfzYgAVAAAAABtztEH5Odp5AOkBYto-pdcpPvE2",
                "CAPTCHA_BASE" to "http://www.prograd.uesc.br"
            ),
            "UNEB" to mapOf(
                "BASE_URL" to "http://www.portalacademico.uneb.br/PortalSagres",
                "LOGIN_VIEW_STATE" to "/wEPDwUKLTEzODQxMjM1NA9kFgJmD2QWBAIBD2QWDAIEDxYCHgRocmVmBT1+L0FwcF9UaGVtZXMvTmV3VGhlbWUvQWNlc3NvRXh0ZXJuby5jc3M/ZnA9NjM3MzU0NDU5NTgwMDAwMDAwZAIFDxYCHwAFOH4vQXBwX1RoZW1lcy9OZXdUaGVtZS9Db250ZXVkby5jc3M/ZnA9NjM3MzU0NDU5NTgwMDAwMDAwZAIGDxYCHwAFOX4vQXBwX1RoZW1lcy9OZXdUaGVtZS9Fc3RydXR1cmEuY3NzP2ZwPTYzNzM1NDQ1OTU4MDAwMDAwMGQCBw8WAh8ABTl+L0FwcF9UaGVtZXMvTmV3VGhlbWUvTWVuc2FnZW5zLmNzcz9mcD02MzczNTQ0NTk1ODAwMDAwMDBkAggPFgIfAAU2fi9BcHBfVGhlbWVzL05ld1RoZW1lL1BvcFVwcy5jc3M/ZnA9NjM3MzU0NDU5NTgwMDAwMDAwZAIJDxYCHwAFXi9Qb3J0YWxTYWdyZXMvUmVzb3VyY2VzL1N0eWxlcy9BcHBfVGhlbWVzL05ld1RoZW1lL05ld1RoZW1lMDEvZXN0aWxvLmNzcz9mcD02MzYxMDU4MjY2NDAwMDAwMDBkAgMPZBYEAgcPDxYEHgRUZXh0BQ1TYWdyZXMgUG9ydGFsHgdWaXNpYmxlaGRkAgsPZBYGAgEPDxYCHwJoZGQCAw88KwAKAQAPFgIeDVJlbWVtYmVyTWVTZXRoZBYCZg9kFgICDw9kFgJmDw8WAh8CaGRkAgUPZBYEZg8WAh8CZ2QCAg9kFgICAQ8WAh4LXyFJdGVtQ291bnQCARYCZg9kFgICAQ8WAh4Fc3R5bGUFDm1heC13aWR0aDo0Y207FgYCAQ8PFgIfAQUNU2FncmVzIE1vYmlsZWRkAgMPDxYEHghJbWFnZVVybAUsfi9SZXNvdXJjZXMvSW1hZ2VzL1FSQ29kZXMvU2FncmVzIE1vYmlsZS5naWYeB1Rvb2xUaXAFKWh0dHA6Ly93d3cucG9ydGFsYWNhZGVtaWNvLnVuZWIuYnIvbW9iaWxlFgIfBQUVaGVpZ2h0OjRjbTt3aWR0aDo0Y207ZAIFDw8WAh8BBSlodHRwOi8vd3d3LnBvcnRhbGFjYWRlbWljby51bmViLmJyL21vYmlsZWRkZBYumhRuIw4YTms2oTFhh6fTbwJq",
                "LOGIN_VW_STT_GEN" to "065EA922",
                "LOGIN_VIEW_VALID" to "/wEdAAQKSEVPdfCrQ/74B7srq70DM4nqN81slLG8uEFL8sVLUjoauXZ8QTl2nEJmPx53FYhjUq3W1Gjeb7bKHHg4dlob/wvZYFkCR9HTWCMsUzs6gTf4At0=",
                "REQUIRES_CAPTCHA" to "false",
                "CAPTCHA_SITE_KEY" to "",
                "CAPTCHA_BASE" to ""
            ),
            "UESB" to mapOf(
                "BASE_URL" to "http://sagres.uesb.br/PortalSagres",
                "LOGIN_VIEW_STATE" to "/wEPDwUKMTc5MDkxMTc2NA9kFgJmD2QWBAIBD2QWDAIEDxYCHgRocmVmBT1+L0FwcF9UaGVtZXMvTmV3VGhlbWUvQWNlc3NvRXh0ZXJuby5jc3M/ZnA9NjM2ODQzNTM2OTgwMDAwMDAwZAIFDxYCHwAFOH4vQXBwX1RoZW1lcy9OZXdUaGVtZS9Db250ZXVkby5jc3M/ZnA9NjM2ODQzNTM2OTgwMDAwMDAwZAIGDxYCHwAFOX4vQXBwX1RoZW1lcy9OZXdUaGVtZS9Fc3RydXR1cmEuY3NzP2ZwPTYzNjg0MzUzNjk4MDAwMDAwMGQCBw8WAh8ABTl+L0FwcF9UaGVtZXMvTmV3VGhlbWUvTWVuc2FnZW5zLmNzcz9mcD02MzY4NDM1MzcwMDAwMDAwMDBkAggPFgIfAAU2fi9BcHBfVGhlbWVzL05ld1RoZW1lL1BvcFVwcy5jc3M/ZnA9NjM2ODQzNTM3MDAwMDAwMDAwZAIJDxYCHwAFXi9Qb3J0YWxTYWdyZXMvUmVzb3VyY2VzL1N0eWxlcy9BcHBfVGhlbWVzL05ld1RoZW1lL05ld1RoZW1lMDEvZXN0aWxvLmNzcz9mcD02MzYxMDU4MjY2NDAwMDAwMDBkAgMPZBYEAgcPDxYEHgRUZXh0BQ1TYWdyZXMgUG9ydGFsHgdWaXNpYmxlaGRkAgsPZBYGAgEPDxYCHwJoZGQCAw88KwAKAQAPFgIeDVJlbWVtYmVyTWVTZXRoZGQCBQ9kFgICAg9kFgICAQ8WAh4LXyFJdGVtQ291bnRmZGT2jq3GdOJCykutzHHj/n4FNUsnUw==",
                "LOGIN_VW_STT_GEN" to "065EA922",
                "LOGIN_VIEW_VALID" to "/wEdAARSewvwfiowtbmcj5TsbyonM4nqN81slLG8uEFL8sVLUjoauXZ8QTl2nEJmPx53FYhjUq3W1Gjeb7bKHHg4dlobnNndlhakCWrxjhOs6ggtYAyWZPU=",
                "REQUIRES_CAPTCHA" to "false",
                "CAPTCHA_SITE_KEY" to "",
                "CAPTCHA_BASE" to ""
            )
        )

        // Sagres Default Endpoints
        private val SAGRES_ENDPOINTS = mapOf(
            "SAGRES_LOGIN_PAGE" to "__REPLACE__UNES__/Acesso.aspx",
            "SAGRES_MESSAGES_PAGE" to "__REPLACE__UNES__/Modules/Diario/Aluno/Consultas/Recados.aspx",
            "SAGRES_GRADE_PAGE" to "__REPLACE__UNES__/Modules/Diario/Aluno/Relatorio/Boletim.aspx",
            "SAGRES_DIARY_PAGE" to "__REPLACE__UNES__/Modules/Diario/Aluno/Default.aspx",
            "SAGRES_CLASS_PAGE" to "__REPLACE__UNES__/Modules/Diario/Aluno/Classe/ConsultaAulas.aspx",
            "SAGRES_GRADE_ANY" to "__REPLACE__UNES__/Modules/Diario/Aluno/Relatorio/Boletim.aspx?op=notas",
            "SAGRES_ENROLL_CERT" to "__REPLACE__UNES__/Modules/Diario/Aluno/Relatorio/ComprovanteMatricula.aspx",
            "SAGRES_FLOWCHART" to "__REPLACE__UNES__/Modules/Diario/Aluno/Relatorio/FluxogramaAluno.aspx",
            "SAGRES_HISTORY" to "__REPLACE__UNES__/Modules/Diario/Aluno/Relatorio/HistoricoEscolar.aspx",
            "SAGRES_DEMAND_OFFERS" to "__REPLACE__UNES__/Modules/Diario/Aluno/Matricula/Demanda.aspx",
            "SAGRES_REQUESTED_SERVICES" to "__REPLACE__UNES__/Modules/Diario/Aluno/Academico/SolicitacaoServico.aspx",
            "SAGRES_ALL_DISCIPLINES_PAGE" to "__REPLACE__UNES__/Modules/Diario/Aluno/Classe/SelecaoClasse.aspx?redirect=__REPLACE__UNES__/Modules/Diario/Aluno/Classe/ConsultaAulas.aspx",
            "SAGRES_SHITTY_URL" to "__REPLACE__UNES__/SessionTimeoutHandler.ashx?action=0"
        )

        @JvmStatic
        val SUPPORTED_INSTITUTIONS = SAGRES_SERVER_CONSTANTS.map { it.key }.toTypedArray()

        private val CACHED_CONSTANTS = mutableMapOf<String, Constants>()
        private val lock = Any()

        @JvmStatic
        fun forInstitution(institution: String): Constants {
            if (SUPPORTED_INSTITUTIONS.contains(institution)) {
                val instance = CACHED_CONSTANTS[institution]
                if (instance != null) return instance
                return synchronized(lock) {
                    val element = Constants(institution)
                    CACHED_CONSTANTS[institution] = element
                    element
                }
            }
            throw IllegalArgumentException("This institution is not supported")
        }
    }

    private fun getPage(): String {
        val constants = SAGRES_SERVER_CONSTANTS[institution.uppercase(Locale.getDefault())]
        return constants?.get("BASE_URL") ?: throw IllegalArgumentException("This institution is not supported")
    }

    fun getUrl(endpoint: String): String {
        val base = getPage()
        val path = SAGRES_ENDPOINTS[endpoint.uppercase()] ?: throw IllegalArgumentException("This endpoint is not supported")
        return path.replace("__REPLACE__UNES__", base)
    }

    fun getParameter(parameter: String): String {
        val constants = SAGRES_SERVER_CONSTANTS[institution.uppercase()]
        return constants?.get(parameter) ?: throw IllegalArgumentException("This parameter is not supported")
    }
}
