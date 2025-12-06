package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope

val LocalLifecycleScope =
    staticCompositionLocalOf<LifecycleCoroutineScope> {
        error("CompositionLocal LifecycleCoroutineScope not present")
    }

val LocalLifecycle =
    staticCompositionLocalOf<Lifecycle> {
        error("CompositionLocal Lifecycle not present")
    }
