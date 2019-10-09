package com.hipo.hipoexceptionsandroid

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Response

// Create class with dagger and use it everywhere and take gson from the dagger also.
class RetrofitExceptionHandler(
    private val gson: Gson,
    private val defaultErrorMessage: String = "An Error Occurred.",
    private val logErrorCodes: IntArray = intArrayOf()
) {

//    fun checkCodeAndLogIfNeeded(response: Response) {
//        var logString = ""
//        val responseBody = response.body!!
//        val contentLength = responseBody.contentLength()
//        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
//        logString += "<-- ${response.code()}${if (response.message().isEmpty()) "" else ' ' + response.message()} ${response.request().url()} (${tookMs}ms${if (!logHeaders) ", $bodySize body" else ""})")
//        if (logErrorCodes.contains(response.code())) {
//            response.headers()
//        }
//    }
//
//    fun parseErrorBody(errorResponse: Response) {
//        errorResponseBody.code()
//        errorResponseBody.
//        errorResponseBody.string()
//    }

    fun<T> convertDetailedError(response: Response<T>): DetailedErrorMessage {
        if (logErrorCodes.contains(response.code())) {
            try {
                Crashlytics.logException(Exception(response.message()))
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

//    private fun getLogMessage(response: Response): String {
//        var logMessage = StringBuilder()
//        val responseBody = response.body()
//        val contentLength = responseBody?.contentLength()
//        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
//        var requestStartMessage =
//        logMessage.append(
//            "<-- ${response.code()}${if (response.message().isEmpty()) "" else ' ' + response.message()} ${response.request().url()} ")
//
//    }
//
//    private fun logHeader(headers: Headers, i: Int) {
//        val value = if (headers.name(i) in headersToRedact) "██" else headers.value(i)
//        logger.log(headers.name(i) + ": " + value)
//    }
}
