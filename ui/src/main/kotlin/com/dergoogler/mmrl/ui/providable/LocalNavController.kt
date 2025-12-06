package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

/**
 * [CompositionLocal] for providing the [NavHostController] within a Compose hierarchy.
 *
 * This allows you to access the current [NavHostController] instance without
 * explicitly passing it down through every level of your composable function tree.
 *
 * To use this, you must provide a [NavHostController] using
 * `CompositionLocalProvider(LocalNavController provides navController) { ... }`
 * higher up in the composition hierarchy, typically where you set up your
 * navigation host.
 *
 * If this [CompositionLocal] is accessed without a value being provided, it will throw an error.
 * This is intentional to ensure that you've properly configured your navigation
 * setup.
 *
 * Example usage:
 *
 * ```kotlin
 * @Composable
 * fun MyScreen(navController: NavHostController) {
 *   CompositionLocalProvider(LocalNavController provides navController) {
 *     MyContent()
 *   }
 * }
 *
 * @Composable
 * fun MyContent() {
 *   val navController = LocalNavController.current
 *   Button(onClick = { navController.navigate("otherScreen") }) {
 *     Text("Go to other screen")
 *   }
 * }
 * ```
 */
val LocalNavController =
    staticCompositionLocalOf<NavHostController> {
        error("CompositionLocal NavController not present")
    }
