package com.hipo.hipoexceptionsandroid

import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import com.google.gson.JsonElement
import retrofit2.Response
import java.lang.StringBuilder

// Create class with dagger and use it everywhere and take gson from the dagger also.
class RetrofitExceptionHandler(
    private val gson: Gson,
    private val crashlytics: Crashlytics? = null,
    private val defaultErrorMessage: String = "An Error Occurred.",
    private val errorCodesToLog: IntArray = intArrayOf()
) {

    /*
        --->
        501 GET http://localhost:56979/
        BODY : null
        HEADERS {
            Accept-Encoding gzip
            Connection Keep-Alive
            Host localhost:56979
            User-Agent okhttp/4.2.1
        }
        <--
     */
    private fun<T> getLogMessage(response: Response<T>): String {
        val logStringBuilder = StringBuilder()

        val networkResponseRequest = response.raw().networkResponse()?.request()

        val responseCode = response.raw().code()
        val requestMethod = networkResponseRequest?.method()
        val requestUrl = networkResponseRequest?.url()
        val responseBody = networkResponseRequest?.body()

        logStringBuilder.appendln("--->")
        logStringBuilder.appendln("$responseCode $requestMethod $requestUrl ")
        logStringBuilder.appendln("BODY : $responseBody ")
        logStringBuilder.appendln("HEADERS { ")
        val headers = networkResponseRequest?.headers()
        headers?.names()?.forEach { headerName ->
            logStringBuilder.appendln("\t$headerName: ${headers[headerName]}")
        }
        logStringBuilder.appendln("}\n<--")

        return logStringBuilder.toString()
    }


    fun<T> parse(response: Response<T>): DetailedErrorMessage {
        if (errorCodesToLog.contains(response.code())) {
            try {
                crashlytics?.core?.logException(Exception(getLogMessage(response)))
            } catch (exception: Exception) {
                println("RetrofitExceptionHandler Crashlytics has been not started yet.")
            }
        }

        val errorBody = response.errorBody()
        return convertJsonToDetailedErrorMessage(errorBody?.string() ?: "")
    }

    private fun convertJsonToDetailedErrorMessage(errorOutputAsJson: String): DetailedErrorMessage {
        val baseErrorModel = gson.fromJson(errorOutputAsJson, BaseErrorModel::class.java)
        val detailedKeyErrorMap = getKeyErrorMap(baseErrorModel.detail)
        val summaryMessageFromMap = detailedKeyErrorMap.values.firstOrNull()?.firstOrNull()
        val fallbackMessage = baseErrorModel.fallbackMessage
        val summaryMessage =
            when {
                summaryMessageFromMap?.isNotBlank() == true -> summaryMessageFromMap
                fallbackMessage?.isNotEmpty() == true -> fallbackMessage
                else -> defaultErrorMessage
            }
        return DetailedErrorMessage(detailedKeyErrorMap, summaryMessage)
    }

    private fun getKeyErrorMap(
        jsonElement: JsonElement?,
        previousKey: String = ""
    ): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()

        if (jsonElement == null || !jsonElement.isJsonObject) {
            return result
        }

        val entrySet = jsonElement.asJsonObject.entrySet()

        entrySet.forEach {
            val currentKey = previousKey + "/" + it.key

            when {
                it.value.isJsonPrimitive -> result[currentKey] = listOf(it.value.asString)

                it.value.isJsonArray -> {
                    val errorMessageList = mutableListOf<String>()
                    it.value.asJsonArray.forEach { jsonElement ->
                        if (jsonElement.isJsonPrimitive) {
                            errorMessageList.add(jsonElement.asString)
                        }
                    }
                    result[currentKey] = errorMessageList
                }

                else -> result.putAll(
                    getKeyErrorMap(it.value.asJsonObject, currentKey)
                )
            }
        }
        return result
    }
}
