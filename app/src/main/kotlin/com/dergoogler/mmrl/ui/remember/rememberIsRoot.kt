package com.dergoogler.mmrl.ui.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.datastore.model.WorkingMode.Companion.isRoot
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun rememberIsRoot(): Boolean {
    val userPreferences = LocalUserPreferences.current
    return remember(userPreferences, PlatformManager.isAlive) {
        userPreferences.workingMode.isRoot && PlatformManager.isAlive
    }
}