package com.dergoogler.mmrl.platform.content

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModuleCompatibility(
    val hasMagicMount: Boolean,
    val canRestoreModules: Boolean,
) : Parcelable {
    val canNotRestoreModules get() = !canRestoreModules
    val hasNotMagicMount get() = !hasMagicMount

    companion object
}
