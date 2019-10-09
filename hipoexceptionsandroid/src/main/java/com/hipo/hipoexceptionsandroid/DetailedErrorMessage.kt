package com.hipo.hipoexceptionsandroid

data class DetailedErrorMessage(
    val detailedKeyErrorMessageMap: Map<String, List<String>>,
    val summaryMessage: String
)
