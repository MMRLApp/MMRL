package com.dergoogler.mmrl.ext

import android.util.Log // For logging
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeoutOrNull

private const val DEFAULT_AWAIT_TIMEOUT_MILLIS = 30000L // Default timeout of 30 seconds

/**
 * A Composable extension function that awaits a [Deferred] value in a lifecycle-aware manner
 * and provides its result as [State].
 *
 * This function is useful for scenarios where you have a [Deferred] computed outside of
 * Compose (e.g., from a ViewModel or a repository) and you want to display its result
 * in your UI when it becomes available.
 *
 * The [LaunchedEffect] ensures that the `await()` call is tied to the Composable's lifecycle.
 * If the Composable leaves the composition while the [Deferred] is still being awaited,
 * the awaiting coroutine will be cancelled.
 *
 * @param T The type of the value held by the [Deferred].
 * @param initial The initial value for the [State] while the [Deferred] is being awaited or if it fails.
 * @param timeoutMillis The maximum time to wait for the [Deferred] to complete.
 *                      If the timeout is reached, the state will remain `initial`.
 * @param key1 An optional key. If this key changes, the [LaunchedEffect] will be relaunched,
 *             and the [Deferred] will be awaited again (if it's a new Deferred instance or still active).
 * @param key2 Another optional key for the same purpose as [key1].
 * @param onError An optional lambda to handle exceptions that occur during `await()`,
 *                excluding `TimeoutCancellationException` which is handled by returning `initial`.
 *                The lambda receives the exception and the current [MutableState] to potentially update it.
 * @return A [State] object that holds the `initial` value until the [Deferred] completes
 *         successfully within the timeout, then updates to the [Deferred]'s result. If an error
 *         occurs or timeout is hit, it remains `initial` or as updated by `onError`.
 */
@Composable
fun <T> Deferred<T>.awaitAsState(
    initial: T,
    timeoutMillis: Long = DEFAULT_AWAIT_TIMEOUT_MILLIS,
    key1: Any? = null,
    key2: Any? = null,
    onError: ((exception: Throwable, state: MutableState<T>) -> Unit)? = null,
): State<T> {
    val state = remember(this, key1, key2) { mutableStateOf(initial) }

    LaunchedEffect(this, key1, key2) {
        try {
            Log.d("awaitAsState", "Awaiting Deferred. Timeout: $timeoutMillis ms. Initial value: $initial")
            val result =
                withTimeoutOrNull(timeoutMillis) {
                    this@awaitAsState.await()
                }

            if (result != null) {
                Log.d("awaitAsState", "Deferred completed with result: $result")
                state.value = result
            } else {
                Log.w("awaitAsState", "Timed out waiting for Deferred to complete. State remains: ${state.value}")
            }
        } catch (e: TimeoutCancellationException) {
            Log.w("awaitAsState", "TimeoutCancellationException while awaiting Deferred. State remains: ${state.value}", e)
        } catch (e: Exception) {
            Log.e("awaitAsState", "Error awaiting Deferred. State remains: ${state.value}", e)
            onError?.invoke(e, state)
        }
    }

    return state
}
