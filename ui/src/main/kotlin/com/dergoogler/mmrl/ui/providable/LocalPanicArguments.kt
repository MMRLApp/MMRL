package com.dergoogler.mmrl.ui.providable

import android.os.Bundle
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * [CompositionLocal] that provides a [Bundle] containing arguments that were present when a
 * navigation action encountered an unrecoverable error (a "panic").
 *
 * This is intended for use by error-reporting mechanisms to provide more context when a
 * navigation error occurs, allowing for easier debugging.
 *
 * When a navigation operation panics (e.g., due to a missing destination), this CompositionLocal
 * can be read to access the arguments that were being passed to the navigation action at the
 * time of the error. This information can help developers understand what data might have been
 * related to the issue.
 *
 * If no panic occurred or the panic did not involve arguments, this CompositionLocal will throw
 * an error when accessed. This behavior is intended to avoid accidentally reading this local
 * in contexts where it's not applicable.
 *
 * **Usage:**
 *
 * Inside an error handling scope, you can use `LocalPanicArguments.current` to obtain the
 * [Bundle] of arguments.
 *
 * ```kotlin
 * try {
 *   // Perform navigation action that might panic.
 * } catch (e: Exception) {
 *   try {
 *       val panicArgs = LocalPanicArguments.current
 *       // Report or log the panic along with the arguments.
 *       Log.e("NavigationPanic", "Navigation panicked", e)
 *       Log.e("NavigationPanic", "Panic Arguments: $panicArgs")
 *   } catch(e: Exception){
 *       //If LocalPanicArguments is not present.
 *       Log.e("NavigationPanic", "Navigation panicked, no panic arguments available", e)
 *   }
 *
 * }
 * ```
 *
 * **Note:** This CompositionLocal is typically populated internally by the navigation library
 * when a panic occurs. It should not be set manually by the application.
 */
val LocalPanicArguments =
    staticCompositionLocalOf<Bundle> {
        error("CompositionLocal NavBackStackEntry.panicArguments not present")
    }
