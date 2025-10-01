package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.viewmodel.SuperUserViewModel

val LocalSuperUserViewModel = staticCompositionLocalOf<SuperUserViewModel> {
    error("CompositionLocal LocalSuperUserViewModel not present")
}