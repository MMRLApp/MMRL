package com.dergoogler.mmrl.ui.screens.moduleView.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.model.online.VersionItem

val LocalModuleViewDownloader = staticCompositionLocalOf<(VersionItem, Boolean) -> Unit> {
    error("CompositionLocal LocalModuleViewDownloader not present")
}