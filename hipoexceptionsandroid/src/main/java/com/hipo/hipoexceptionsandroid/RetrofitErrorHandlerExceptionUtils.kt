package com.hipo.hipoexceptionsandroid

import android.util.Log
import com.crashlytics.android.Crashlytics

class FallbackMessageException(requestDetail: String) : Exception(requestDetail)
class NoFallbackMessageException(requestDetail: String) : Exception(requestDetail)
class UnexpectedResponseCodeException(requestDetail: String) : Exception(requestDetail)

private const val LIBRARY_TAG = "RetrofitErrorHandler"

fun sendExceptionLog(exception: Exception) {
    try {
        Crashlytics.logException(exception)
    } catch (exception: Exception) {
        Log.w(LIBRARY_TAG, "Crashlytics has been not started yet.")
    }
}
