package com.dergoogler.mmrl.compat

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.io.File
import java.io.OutputStream
import java.util.Locale

object NetworkCompat {
    private var cacheDirOrNull: File? = null
    private val cacheOrNull: Cache?
        get() =
            cacheDirOrNull?.let {
                Cache(File(it, "okhttp"), 10 * 1024 * 1024)
            }

    val defaultJson = Json { ignoreUnknownKeys = true }

    fun setCacheDir(dir: File) {
        cacheDirOrNull = dir
    }

    fun isHTML(text: String) =
        "<html\\s*>|<head\\s*>|<body\\s*>|<!doctype\\s*html\\s*>"
            .toRegex()
            .containsMatchIn(text)

    fun isUrl(url: String) = url.toHttpUrlOrNull() != null

    fun isBlobUrl(url: String) =
        "https://github.com/[^/]+/[^/]+/blob/.+"
            .toRegex()
            .matches(url)

    private fun createOkHttpClient(
        debug: Boolean = false,
        userAgent: String = "NetworkCompat/0x0",
    ): OkHttpClient {
        val builder = OkHttpClient.Builder().cache(cacheOrNull)

        if (debug) {
            builder.addInterceptor(
                HttpLoggingInterceptor { Log.d(TAG, it) }
                    .apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
            )
        } else {
            builder.connectionSpecs(listOf(ConnectionSpec.MODERN_TLS))
        }

        builder.addInterceptor { chain ->
            val request = chain.request().newBuilder()
            request.header("User-Agent", userAgent)
            request.header("Accept-Language", Locale.getDefault().toLanguageTag())
            chain.proceed(request.build())
        }

        return builder.build()
    }

    fun createRetrofit(): Retrofit.Builder {
        val client = createOkHttpClient()

        return Retrofit
            .Builder()
            .addConverterFactory(
                defaultJson.asConverterFactory("application/json; charset=UTF8".toMediaType()),
            ).client(client)
    }

    inline fun <reified T : Any> createApi(url: String) =
        createRetrofit()
            .baseUrl(url)
            .build()
            .create<T>()

    suspend fun <T> request(
        url: String,
        converter: (ResponseBody, Headers) -> T,
    ) = runRequest(converter = converter) {
        val client = createOkHttpClient()
        val request =
            Request
                .Builder()
                .url(url)
                .build()

        client
            .newCall(request)
            .execute()
    }

    suspend fun requestString(url: String) =
        request(
            url = url,
            converter = { body, _ ->
                body.string()
            },
        )

    @OptIn(ExperimentalSerializationApi::class)
    suspend inline fun <reified T> requestJson(url: String) =
        request(
            url = url,
            converter = { body, _ ->
                defaultJson.decodeFromStream<T>(body.byteStream())
            },
        )

    suspend fun download(
        url: String,
        output: OutputStream,
        onProgress: (Float) -> Unit,
    ) = request(url) { body, _ ->
        val buffer = ByteArray(2048)
        val input = body.byteStream()

        val all = body.contentLength()
        var finished: Long = 0
        var readying: Int

        while (input.read(buffer).also { readying = it } != -1) {
            output.write(buffer, 0, readying)
            finished += readying.toLong()

            val progress = (finished * 1.0 / all).toFloat()
            onProgress(progress)
        }

        output.flush()
        output.close()
        input.close()
    }

    suspend fun <T> runRequest(block: () -> retrofit2.Response<T>): Result<T> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = block()
                require(response.isSuccessful) {
                    val error = response.errorBody()?.string()
                    if (error?.let(::isHTML) == false) {
                        error
                    } else {
                        "status = ${response.code()}"
                    }
                }

                requireNotNull(response.body())
            }
        }

    suspend fun <T> runRequest(
        converter: (ResponseBody, Headers) -> T,
        block: () -> okhttp3.Response,
    ): Result<T> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = block()
                val body = requireNotNull(response.body)
                require(response.isSuccessful) {
                    val error = body.string()
                    if (!isHTML(error)) {
                        error
                    } else {
                        "status = ${response.code}"
                    }
                }

                converter(body, response.headers)
            }
        }

    @Suppress("UNCHECKED_CAST")
    object Compose {
        data class Value(
            private val inner: Any?,
            private val event: Event,
        ) {
            constructor(result: Result<Any>) : this(
                inner =
                    when {
                        result.isSuccess -> result.getOrNull()
                        result.isFailure -> result.exceptionOrNull()
                        else -> null
                    },
                event =
                    when {
                        result.isSuccess -> Event.Succeeded
                        result.isFailure -> Event.Failed
                        else -> Event.Loading
                    },
            )

            val isLoading by lazy { event == Event.Loading }
            val isSuccess by lazy { event == Event.Succeeded }
            val isFailure by lazy { event == Event.Failed }

            fun <T> data() = inner as T

            fun error() = inner as? Throwable

            enum class Event {
                Loading,
                Succeeded,
                Failed,
            }

            companion object {
                fun none() = Value(null, Event.Loading)
            }
        }

        @Composable
        fun <T> runRequest(block: suspend () -> Result<T>): Value {
            var value: Value by remember { mutableStateOf(Value.none()) }
            LaunchedEffect(true) { value = Value(block() as Result<Any>) }
            return value
        }

        @Composable
        fun requestString(url: String) =
            runRequest(
                block = { NetworkCompat.requestString(url) },
            )

        @Composable
        inline fun <reified T> requestJson(url: String) =
            runRequest(
                block = { NetworkCompat.requestJson<T>(url) },
            )
    }

    private const val TAG = "NetworkCompat"
}
