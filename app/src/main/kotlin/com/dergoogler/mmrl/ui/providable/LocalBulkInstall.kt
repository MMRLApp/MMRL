package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.viewmodel.BulkInstallViewModel

val LocalBulkInstall =
    staticCompositionLocalOf<BulkInstallViewModel> {
        error("CompositionLocal BulkInstallViewModel not present")
    }
