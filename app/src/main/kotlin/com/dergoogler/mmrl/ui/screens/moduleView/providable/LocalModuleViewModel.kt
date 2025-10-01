package com.dergoogler.mmrl.ui.screens.moduleView.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.viewmodel.ModuleViewModel

val LocalModuleViewModel = staticCompositionLocalOf<ModuleViewModel> {
    error("CompositionLocal LocalModuleViewModel not present")
}