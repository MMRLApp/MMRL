package com.dergoogler.mmrl.ext.compose.providable

import android.app.Activity
import androidx.compose.runtime.staticCompositionLocalOf

val LocalActivity = staticCompositionLocalOf<Activity> {
    error("CompositionLocal Activity not present")
}