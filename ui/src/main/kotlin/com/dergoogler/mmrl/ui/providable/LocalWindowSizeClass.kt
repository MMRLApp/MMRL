package com.dergoogler.mmrl.ui.providable

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.staticCompositionLocalOf

val LocalWindowSizeClass =
    staticCompositionLocalOf<WindowSizeClass> {
        error("CompositionLocal LocalWindowSizeClass not present")
    }