package com.hipo.hipoexceptionsandroid

import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import java.io.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.logging.Level
import java.util.logging.Logger

class SampleDataUnitTest {

    interface TestService {
        @GET("/")
        fun getString(): Call<String>
    }

    private var server = MockWebServer()
    private lateinit var testService: TestService

    private val defaultErrorMessage = "New Generic - An Error Occured"

    private val retrofitExceptionHandler =
        RetrofitExceptionHandler(
            Gson(),
            defaultErrorMessage,
            intArrayOf(402)
        )

    @Before
    fun setup() {
        Logger.getLogger(MockWebServer::class.java.name).level = Level.WARNING
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        testService = retrofit.create(TestService::class.java)
    }

    @Test
    fun parseSimpleErrorMessage() {
        val errorBodyAsString = getJsonFileAsString("simple_error_message.json")

        server.enqueue(
            MockResponse()
                .setResponseCode(402)
                .setBody(errorBodyAsString)
        )

        val response = testService.getString().execute()

        retrofitExceptionHandler.convertDetailedError(response).run {
            assert(summaryMessage == "Email is already registered. (not fallback)")
            assert(detailedKeyErrorMessageMap.size == 1)
        }
    }

    @Test
    fun parseSimpleDoubleErrorMessage() {
        val errorBodyAsString = getJsonFileAsString("simple_double_error_message.json")

        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody(errorBodyAsString)
        )

        val response = testService.getString().execute()

        retrofitExceptionHandler.convertDetailedError(response).run {
            assert(summaryMessage == "A valid integer is required. (not fallback)")
            assert(detailedKeyErrorMessageMap.size == 2)
        }
    }


    @Test
    fun parseOnlyFallbackMessage() {
        val errorBodyAsString = getJsonFileAsString("only_fallback_message.json")

        server.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody(errorBodyAsString)
        )

        val response = testService.getString().execute()

        retrofitExceptionHandler.convertDetailedError(response).run {
            assert(summaryMessage == "Email is already registered.")
            assert(detailedKeyErrorMessageMap.isEmpty())
        }
    }


    @Test
    fun parseNonFieldErrorMessage() {
        val errorBodyAsString = getJsonFileAsString("non_field_error_message.json")

        server.enqueue(
            MockResponse()
                .setResponseCode(501)
                .setBody(errorBodyAsString)
        )

        val response = testService.getString().execute()

        retrofitExceptionHandler.convertDetailedError(response).run {
            assert(summaryMessage == "Profile credentials are not correct. (not fallback)")
            assert(detailedKeyErrorMessageMap.size == 1)
            assert(detailedKeyErrorMessageMap["/non_field_errors"]?.size == 2)
        }
    }

    @Test
    fun parseNestedErrorMessage() {
        val errorBodyAsString = getJsonFileAsString("nested_error_message.json")

        server.enqueue(
            MockResponse()
                .setResponseCode(502)
                .setBody(errorBodyAsString)
        )

        val response = testService.getString().execute()

        retrofitExceptionHandler.convertDetailedError(response).run {
            assert(summaryMessage == "Amount Type is False. (not fallback)")
            assert(detailedKeyErrorMessageMap.size == 1)
        }
    }

    @Test
    fun parseNestedDoubleErrorMessage() {
        val errorBodyAsString = getJsonFileAsString("nested_double_error_message.json")

        server.enqueue(
            MockResponse()
                .setResponseCode(503)
                .setBody(errorBodyAsString)
        )

        val response = testService.getString().execute()

        retrofitExceptionHandler.convertDetailedError(response).run {
            assert(summaryMessage == "Amount Type is False. (not fallback)")
            assert(detailedKeyErrorMessageMap.size == 2)
        }
    }

    @Test
    fun parseTooMuchNestedErrorMessage() {
        val errorBodyAsString = getJsonFileAsString("too_much_nested_error_message.json")

        server.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody(errorBodyAsString)
        )

        val response = testService.getString().execute()

        retrofitExceptionHandler.convertDetailedError(response).run {
            assert(summaryMessage == "Error. (not fallback)")
            assert(detailedKeyErrorMessageMap.size == 3)
        }
    }

    @Test
    fun parseEmptyErrorMessage() {
        val errorBodyAsString = getJsonFileAsString("empty.json")

        server.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody(errorBodyAsString)
        )

        val response = testService.getString().execute()

        retrofitExceptionHandler.convertDetailedError(response).run {
            assert(summaryMessage == defaultErrorMessage)
            assert(detailedKeyErrorMessageMap.isEmpty())
        }
    }

    @Test
    fun parseNullSafety() {
        val errorBodyAsString = getJsonFileAsString("null_safety.json")

        server.enqueue(
            MockResponse()
                .setResponseCode(403)
                .setBody(errorBodyAsString)
        )

        val response = testService.getString().execute()

        retrofitExceptionHandler.convertDetailedError(response).run {
            assert(summaryMessage == defaultErrorMessage)
            assert(detailedKeyErrorMessageMap.isEmpty())
        }
    }

    private fun getJsonFileAsString(filename: String): String {
        return File(SAMPLE_DATA_BASE_PATH + filename).readText()
    }

    companion object {
        const val SAMPLE_DATA_BASE_PATH =
            "./src/test/java/com/hipo/hipoexceptionsandroid/sampledata/"
    }
}
