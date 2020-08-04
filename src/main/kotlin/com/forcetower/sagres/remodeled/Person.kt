package com.forcetower.sagres.remodeled

import com.google.gson.annotations.SerializedName

data class Person(
    val id: Long,
    @SerializedName("nome")
    val name: String,
    @SerializedName("tipoPessoa")
    val personKind: String,
    val cpf: String?,
    val email: String?
)
