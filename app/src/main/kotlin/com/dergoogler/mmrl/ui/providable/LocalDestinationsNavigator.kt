package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

val LocalDestinationsNavigator = staticCompositionLocalOf<DestinationsNavigator> {
    error("CompositionLocal DestinationsNavigator not present")
}