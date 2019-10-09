package com.hipo.hipoexceptionsandroid

import android.util.Log
import com.crashlytics.android.Crashlytics
import java.lang.Exception

class FallbackMessageException(requestDetail: String): Exception(requestDetail)
class NoFallbackMessageException(requestDetail: String): Exception(requestDetail)
class UnexpectedResponseCodeException(requestDetail: String): Exception(requestDetail)

fun sendExceptionLog(exception: Exception) {
    try {
        Crashlytics.logException(exception)
    } catch (exception: Exception) {
        Log.w("RetrofitErrorHandler", "Crashlytics has been not started yet.")
    }
}
