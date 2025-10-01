package com.dergoogler.mmrl.utils

import android.os.SystemProperties
import dev.dergoogler.mmrl.compat.BuildCompat

object BlurUtil {
    fun isBlurDisabledBySystem(): Boolean {
        val disableBlurs = SystemProperties.getBoolean("persist.sysui.disable_blurs", false)
        val blursExpensive = SystemProperties.getBoolean("ro.sf.blurs_are_expensive", false)
        return disableBlurs || blursExpensive
    }

    fun isBlurSupported(): Boolean {
        return  BuildCompat.atLeastS && !isBlurDisabledBySystem()
    }
}