package com.dergoogler.mmrl.network

inline fun <reified T> runRequest(run: () -> retrofit2.Response<T>): Result<T> =
    try {
        val response = run()
        if (response.isSuccessful) {
            val data = response.body()
            if (data != null) {
                Result.success(data)
            } else {
                Result.failure(NullPointerException())
            }
        } else {
            val errorBody = response.errorBody()
            val error = errorBody?.string() ?: "404 Not Found"

            if (NetworkUtils.isHTML(error)) {
                Result.failure(RuntimeException("404 Not Found"))
            } else {
                Result.failure(RuntimeException(error))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

inline fun <reified T> runRequest(
    get: (okhttp3.ResponseBody, okhttp3.Headers) -> T,
    run: () -> okhttp3.Response,
): Result<T> =
    try {
        val response = run()
        val body = response.body
        val headers = response.headers
        if (response.isSuccessful) {
            if (body != null) {
                Result.success(get(body, headers))
            } else {
                Result.failure(NullPointerException())
            }
        } else {
            val error = body?.string() ?: "404 Not Found"

            if (NetworkUtils.isHTML(error)) {
                Result.failure(RuntimeException("404 Not Found"))
            } else {
                Result.failure(RuntimeException(error))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
