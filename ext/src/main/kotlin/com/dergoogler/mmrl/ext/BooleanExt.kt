package com.dergoogler.mmrl.ext

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Executes the given [block] if the Boolean value is `true`.
 *
 * This function is an inline extension function for nullable Booleans (`Boolean?`).
 * It checks if the Boolean value is exactly `true`. If it is, the provided `block`
 * is executed with the Boolean value (which will be `true`) as its argument.
 *
 * Example:
 * ```kotlin
 * val isLoggedIn: Boolean? = true
 * isLoggedIn.takeTrue {
 *     println("User is logged in.")
 * }
 *
 * val isActive: Boolean? = false
 * isActive.takeTrue {
 *     // This block will not be executed
 *     println("User is active.")
 * }
 *
 * val userStatus: Boolean? = null
 * userStatus.takeTrue {
 *     // This block will not be executed
 *     println("User status is true.")
 * }
 * ```
 *
 * @param block A lambda function that takes a non-null Boolean (which will be `true`) as input and returns Unit.
 *              This block is executed only if the receiver Boolean is `true`.
 */
inline fun Boolean?.takeTrue(block: (Boolean) -> Unit) {
    if (this == true) {
        block(this)
    }
}

/**
 * Remembers the Boolean value and executes the given [block] if the remembered value is `true`.
 *
 * This Composable inline extension function is designed for use within Jetpack Compose.
 * It uses `remember` to store the initial Boolean value based on the provided `keys`.
 * If the remembered Boolean value is `true`, the `block` is executed with that `true` value.
 *
 * This can be useful for conditionally executing Composable code or side effects
 * based on a Boolean state that should only trigger the `block` when it's `true`
 * and its dependencies (`keys`) change.
 *
 * The `@SuppressLint("ComposableNaming")` is used because this function, while being
 * a Composable, doesn't follow the typical PascalCase naming convention for Composable functions.
 *
 * @param keys A vararg of keys that `remember` will use. If any of these keys change,
 *             the Boolean value will be re-evaluated and remembered.
 * @param block A lambda function that takes a non-null Boolean (which will be `true`) as input
 *              and returns Unit. This block is executed only if the remembered Boolean is `true`.
 */
@SuppressLint("ComposableNaming")
@Composable
inline fun Boolean?.rememberTrue(
    vararg keys: Any?,
    block: (Boolean) -> Unit,
) {
    val rem = remember(keys) { this }
    if (rem == true) {
        block(rem)
    }
}

/**
 * Executes the given [block] if this Boolean is `false`.
 *
 * @param block The block to execute. The receiver Boolean (which is `false`) is passed as its argument.
 */
inline fun Boolean?.takeFalse(block: (Boolean) -> Unit) {
    if (this == false) {
        block(this)
    }
}

@OptIn(ExperimentalContracts::class)
fun Boolean?.isNullOrFalse(): Boolean {
    contract {
        returns(true) implies (this@isNullOrFalse == null)
    }

    return this == null || this == false

}

fun Boolean?.isNotNullOrFalse() = !isNullOrFalse()