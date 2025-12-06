package com.dergoogler.mmrl.ext

import android.os.Bundle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder
import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.ext.moshi.moshi
import kotlinx.serialization.json.Json

/**
 * Retrieves the arguments associated with the current navigation back stack entry.
 * If the arguments are null, throws a [BrickException] with a descriptive error message.
 *
 * @throws BrickException if the arguments are null.
 * @return The arguments of the current navigation back stack entry.
 */
val NavBackStackEntry.panicArguments get() = arguments ?: throw BrickException("Arguments are null")

/**
 * Retrieves the string associated with the given key from the Bundle and decodes it as a URL.
 * If the key is found, the string is decoded; otherwise, it returns null.
 *
 * @param key The key used to retrieve the value from the Bundle.
 * @param force If true, will force the URL decoding even if the value is null.
 * @return The decoded string associated with the given key, or null if the key does not exist.
 */
fun Bundle.loadString(
    key: String,
    force: Boolean = false,
): String? = this.getString(key)?.toDecodedUrl(force)

/**
 * Retrieves the string associated with the given key from the Bundle and decodes it as a URL.
 * If the key is not found or the value is null, throws a [BrickException] with a descriptive error message.
 *
 * @param key The key used to retrieve the value from the Bundle.
 * @param force If true, will force the URL decoding even if the value is null.
 * @throws BrickException if the key is not found or if the value is null.
 * @return The decoded string associated with the given key.
 */
fun Bundle.panicString(
    key: String,
    force: Boolean = false,
): String = this.loadString(key, force) ?: throw BrickException("Key '$key' is null")

inline fun <reified T : Any> Bundle.panicJson(key: String): T = Json.decodeFromString<T>(panicString(key))

inline fun <reified T> Bundle.panicMoshiParcelable(key: String): T {
    val json = panicString(key)
    val jsonAdapter = moshi.adapter(T::class.java)
    return jsonAdapter.fromJson(json) ?: throw BrickException("Unable to parse json")
}

fun NavController.navigateSingleTopTo(
    route: String,
    launchSingleTop: Boolean = true,
    builder: NavOptionsBuilder.() -> Unit = {},
) = navigate(
    route = route,
) {
    this.launchSingleTop = launchSingleTop
    restoreState = true
    builder()
}

fun NavController.navigateSingleTopTo(
    route: String,
    args: Map<String, String> = emptyMap(),
    launchSingleTop: Boolean = true,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    var modifiedRoute = route
    args.forEach { (key, value) ->
        modifiedRoute = modifiedRoute.replace("{$key}", value.toEncodedUrl())
    }

    navigate(modifiedRoute) {
        this.launchSingleTop = launchSingleTop
        restoreState = true
        builder()
    }
}

fun <T : Any> NavController.navigateSingleTopTo(
    route: T,
    launchSingleTop: Boolean = true,
    builder: NavOptionsBuilder.() -> Unit = {},
) = navigate(route) {
    this.launchSingleTop = launchSingleTop
    restoreState = true
    builder()
}

fun NavController.navigatePopUpTo(
    route: String,
    launchSingleTop: Boolean = true,
    restoreState: Boolean = true,
    inclusive: Boolean = true,
) = navigateSingleTopTo(
    route = route,
) {
    popUpTo(
        id = currentDestination?.parent?.id ?: graph.findStartDestination().id,
    ) {
        this.saveState = restoreState
        this.inclusive = inclusive
    }
    this.launchSingleTop = launchSingleTop
    this.restoreState = restoreState
}
