package com.hipo.hipoexceptionsandroid

data class ParsedError(
    val keyErrorMessageMap: Map<String, List<String>>,
    val message: String,
    val responseCode: Int,
    val type: String
)
