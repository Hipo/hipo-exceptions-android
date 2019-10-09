package com.hipo.hipoexceptionsandroid

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class BaseErrorModel(
    @SerializedName("type") val type: String? = null,
    @SerializedName("detail") val detail: JsonElement? = null,
    @SerializedName("fallback_message") val fallbackMessage: String? = null
)
