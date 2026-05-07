package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.viewmodel.SettingsViewModel

val LocalSettings =
    staticCompositionLocalOf<SettingsViewModel> {
        error("CompositionLocal SettingsViewModel not present")
    }
