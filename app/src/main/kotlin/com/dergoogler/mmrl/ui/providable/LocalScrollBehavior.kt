package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import dev.mmrlx.compose.ui.toolbar.ToolbarScrollBehavior

val LocalScrollBehavior =
    staticCompositionLocalOf<ToolbarScrollBehavior> {
        error("CompositionLocal LocalScrollBehavior not present")
    }
