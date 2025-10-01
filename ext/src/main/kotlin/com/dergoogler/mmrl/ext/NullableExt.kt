package com.dergoogler.mmrl.ext

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Returns the provided parameter if the receiver is non-null; otherwise, returns null.
 *
 * @param param The value to return if the receiver is non-null.
 * @return The parameter value if the receiver is non-null, or null otherwise.
 */
infix fun <T> Any?.nullable(param: T): T? = if (this != null) param else null

infix fun <T> List<T>?.empty(param: List<T>): List<T> = if (this != null) param else emptyList()

/**
 * Safely invokes a block of code on a nullable receiver if it's not null, otherwise returns null.
 *
 * This infix function provides a concise way to execute a lambda expression (`block`)
 * only when the receiver object (`this`) is not null. If the receiver is null,
 * the function immediately returns null, preventing a NullPointerException.
 *
 * This is similar to the safe call operator (`?.`), but allows for more complex
 * operations within the block that might not be directly achievable with a simple
 * safe call.
 *
 * @param T The type of the nullable receiver.
 * @param R The return type of the block.
 * @param block A lambda expression (extension function) that will be executed if the receiver is not null.
 *              The receiver object (`this`) is available within the block's scope.
 * @return The result of executing the `block` if the receiver is not null, otherwise `null`.
 *
 * @sample
 * ```kotlin
 * data class Person(val name: String, val age: Int)
 *
 * fun main() {
 *     val person1: Person? = Person("Alice", 30)
 *     val person2: Person? = null
 *
 *     // Example 1: Accessing properties and performing operations
 *     val greeting1 = person1.nullvoke { "Hello, ${this.name}! You are ${this.age} years old." }
 *     println(greeting1) // Output: Hello, Alice! You are 30 years old.
 *
 *     val greeting2 = person2.nullvoke { "Hello, ${this.name}! You are ${this.age} years old." }
 *     println(greeting2) // Output: null
 *
 *     // Example 2: Calling methods
 *     val upperCaseName1 = person1.nullvoke { name.toUpperCase() }
 *     println(upperCaseName1) // Output: ALICE
 *
 *     val upperCaseName2 = person2.nullvoke { name.toUpperCase() }
 *     println(upperCaseName2) // Output: null
 *
 *     // Example 3: When the block itself might return null
 */
infix fun <T, R> T?.nullvoke(block: T.() -> R): R? = if (this != null) block(this) else null

/**
 * Returns the provided parameter if the Boolean receiver is non-null and true; otherwise, returns null.
 *
 * @param param The value to return if the receiver is non-null and true.
 * @return The parameter value if the receiver is non-null and true, or null otherwise.
 */
infix fun <T> Boolean?.nullable(param: T): T? = if (this != null && this) param else null

/**
 * Runs the block if the receiver is non-null and returns its result; otherwise, returns null.
 *
 * @param block The function to run if the receiver is non-null.
 * @return The result of the block or null.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null) block(this) else null
}

/**
 * Remembers the result of the [block] if the receiver `this` is not null.
 * If the receiver `this` is null, it returns null.
 *
 * This Composable function is useful for remembering a value derived from a nullable
 * object, ensuring that the calculation in the [block] is only performed if the object
 * is not null and the result is remembered across recompositions.
 *
 * If the nullable receiver `this` changes from a non-null value to null,
 * the function will return null. If it changes from null to a non-null value,
 * the [block] will be executed, and its result will be remembered.
 *
 * @param T The type of the nullable receiver.
 * @param R The type of the result produced by the [block].
 * @param block A lambda function that takes the non-null receiver `T` as an argument
 *              and returns a value of type `R`. This block is only executed if the
 *              receiver is not null.
 * @return The remembered result of the [block] if the receiver is not null, otherwise null.
 */
@Composable
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.rememberNullable(block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    val value = remember { this }

    return if (value != null) block(value) else null
}



/**
 * Applies the given [block] to the receiver if it is not null, and returns the receiver itself.
 * If the receiver is null, returns null. This is similar to the standard library's `apply`
 * function, but specifically for nullable types.
 *
 * @param block A function literal with a receiver of type [T] that performs some operations on it.
 * @return The receiver [T] if it's not null and the block was applied, or null otherwise.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> T?.nullply(block: T.() -> Unit): T? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null) {
        block()
        this
    } else null
}

/**
 * Runs the block if the receiver is non-null and returns its result; otherwise, returns the default value.
 *
 * @param default The value to return if the receiver is null.
 * @param block The function to run if the receiver is non-null.
 * @return The result of the block or the default value.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(default: R, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null) block(this) else default
}

/**
 * Runs the block if the receiver is non-null and returns its result;
 * otherwise, returns the result of the default function.
 *
 * @param default The function to run if the receiver is null.
 * @param block The function to run if the receiver is non-null.
 * @return The result of the block or the result of the default function.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(default: () -> R, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null) block(this) else default()
}

/**
 * Runs the block if the receiver is non-null and the condition is true; otherwise, returns null.
 *
 * @param condition A condition that must be true for the block to run.
 * @param block The function to run if the receiver is non-null and the condition is true.
 * @return The result of the block or null.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(condition: Boolean, block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (condition && this != null) block(this) else null
}

/**
 * Runs the block if the receiver is non-null and satisfies the given condition; otherwise, returns null.
 *
 * @param condition A function to check if the receiver satisfies a condition.
 * @param block The function to run if the receiver is non-null and satisfies the condition.
 * @return The result of the block if the condition is met, or null otherwise.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(condition: (T) -> Boolean, block: (T) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null && condition(this)) block(this) else null
}

/**
 * Runs the block if the receiver is non-null and the condition is true; otherwise, returns the default value.
 *
 * @param condition A condition that must be true for the block to run.
 * @param default The value to return if the receiver is null or the condition is false.
 * @param block The function to run if the receiver is non-null and the condition is true.
 * @return The result of the block, or the default value.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(condition: Boolean, default: R, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (condition && this != null) block(this) else default
}

/**
 * Runs the block if the receiver is non-null and satisfies the given condition; otherwise, returns the default value.
 *
 * @param condition A function to check if the receiver satisfies a condition.
 * @param default The value to return if the receiver is null or does not satisfy the condition.
 * @param block The function to run if the receiver is non-null and satisfies the condition.
 * @return The result of the block if the condition is met, or the default value otherwise.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T, R> T?.nullable(condition: (T) -> Boolean, default: R, block: (T) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (this != null && condition(this)) block(this) else default
}
