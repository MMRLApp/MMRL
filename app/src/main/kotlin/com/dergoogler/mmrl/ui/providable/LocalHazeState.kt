package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import dev.chrisbanes.haze.HazeState

val LocalHazeState = staticCompositionLocalOf<HazeState> {
    error("CompositionLocal LocalHazeState not present")
}