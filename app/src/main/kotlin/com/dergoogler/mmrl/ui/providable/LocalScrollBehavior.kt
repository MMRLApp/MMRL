package com.dergoogler.mmrl.ui.providable

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.staticCompositionLocalOf

val LocalScrollBehavior =
    staticCompositionLocalOf<TopAppBarScrollBehavior> {
        error("CompositionLocal LocalScrollBehavior not present")
    }
