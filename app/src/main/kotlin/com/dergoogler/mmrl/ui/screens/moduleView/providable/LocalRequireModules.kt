package com.dergoogler.mmrl.ui.screens.moduleView.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.model.online.OnlineModule

val LocalRequireModules = staticCompositionLocalOf<List<OnlineModule>> {
    error("CompositionLocal LocalRequireModules not present")
}