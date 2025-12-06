package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalMainNavController =
    staticCompositionLocalOf<NavHostController> {
        error("CompositionLocal NavController not present")
    }
