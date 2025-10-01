package com.dergoogler.mmrl.ext

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@SuppressLint("ComposableNaming")
@Composable
inline fun <reified T, reified S> Any?.thenComposeInvoke(crossinline block: @Composable S.(T) -> Unit): (@Composable S.() -> Unit)? {
    return if (this != null) {
        { block(this, this@thenComposeInvoke as T) }
    } else {
        null
    }
}

@SuppressLint("ComposableNaming")
@Composable
inline fun <reified T, reified S, reified A> Any?.thenComposeInvoke(
    crossinline block: @Composable S.(T, A) -> Unit,
): (@Composable S.(A) -> Unit)? {
    return if (this != null) {
        { arg: A -> block(this, this@thenComposeInvoke as T, arg) }
    } else {
        null
    }
}

@SuppressLint("ComposableNaming")
@Composable
inline fun <reified T, reified S, reified A> Any?.thenComposeInvoke(
    statement: Boolean,
    crossinline block: @Composable S.(T, A) -> Unit,
): (@Composable S.(A) -> Unit)? {
    return if (statement && this != null) {
        { arg: A -> block(this, this@thenComposeInvoke as T, arg) }
    } else {
        null
    }
}

@SuppressLint("ComposableNaming")
@Composable
inline fun <reified T> Any?.thenComposeInvoke(crossinline block: @Composable (T) -> Unit): (@Composable () -> Unit)? {
    return if (this != null) {
        { block(this@thenComposeInvoke as T) }
    } else {
        null
    }
}

@SuppressLint("ComposableNaming")
@Composable
inline fun <reified T> Any?.thenComposeInvoke(
    statement: Boolean,
    crossinline block: @Composable (T) -> Unit,
): (@Composable () -> Unit)? {
    return if (statement && this != null) {
        { block(this@thenComposeInvoke as T) }
    } else {
        null
    }
}

@SuppressLint("ComposableNaming")
@Composable
inline fun <reified T> Any?.thenCompose(crossinline block: @Composable (T) -> Unit): Unit? =
    this.thenComposeInvoke<T>(block)?.invoke()
//
//@SuppressLint("ComposableNaming")
//@Composable
//inline fun <reified T, reified A> Any?.thenCompose(crossinline block: @Composable (T, A) -> Unit): Unit? = this.thenComposeInvoke<T, A>(block)?.invoke(this@thenCompose as A)

// @SuppressLint("ComposableNaming")
// @Composable
// inline fun <reified T, reified S> Any?.compose(crossinline block: @Composable S.(T) -> Unit): Unit? = this.composeInvoke<T, S>(block)?.invoke(this@compose as S)

@OptIn(ExperimentalContracts::class)
fun Any?.isNotNull(): Boolean {
    contract {
        returns(true) implies (this@isNotNull != null)
    }

    return this != null
}

@OptIn(ExperimentalContracts::class)
fun Any?.isNull(): Boolean {
    contract {
        returns(true) implies (this@isNull == null)
    }

    return this == null
}

@Composable
inline fun <T> T.composeApply(block: @Composable T.() -> Unit): T {
    block()
    return this
}

/**
 * # Requires Kotlin Reflection
 */
inline fun <reified T : Any> T.toMap(): Map<String, Any?> {
    val props = T::class.memberProperties.associateBy { it.name }
    return props.keys.associateWith { props[it]?.get(this) }
}

/**
 * # Requires Kotlin Reflection
 */
inline fun <reified T : Any> Map<String, Any?>.toDataClass(): T {
    val ctor = T::class.primaryConstructor
        ?: throw IllegalArgumentException("No primary constructor found for ${T::class}")

    val args = ctor.parameters.associateWith { param ->
        this[param.name]
    }

    return ctor.callBy(args)
}