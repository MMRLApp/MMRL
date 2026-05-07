package com.dergoogler.mmrl.network.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.network.NetworkUtils
import timber.log.Timber

@Composable
fun <T> runRequest(
    get: suspend () -> Result<T>,
    onFailure: (Throwable) -> Unit = {},
    onSuccess: (T) -> Unit,
): Event {
    var event by remember { mutableStateOf(Event.LOADING) }

    LaunchedEffect(null) {
        get()
            .onSuccess {
                event = Event.SUCCEEDED
                onSuccess(it)
            }.onFailure {
                event = Event.FAILED
                onFailure(it)

                Timber.e(it)
            }
    }

    return event
}

@Composable
fun requestString(
    url: String?,
    onFailure: (Throwable) -> Unit = {},
    onSuccess: (String) -> Unit,
): Event {
    if (url.isNullOrBlank()) return Event.FAILED
    return runRequest(
        get = { NetworkUtils.requestString(url) },
        onSuccess = onSuccess,
        onFailure = onFailure,
    )
}

@Composable
inline fun <reified T> requestJson(
    url: String,
    noinline onFailure: (Throwable) -> Unit = {},
    noinline onSuccess: (T) -> Unit,
) = runRequest(
    get = { NetworkUtils.requestJson(url) },
    onSuccess = onSuccess,
    onFailure = onFailure,
)
