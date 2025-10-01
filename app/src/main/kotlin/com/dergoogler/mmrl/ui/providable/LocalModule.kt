package com.dergoogler.mmrl.ui.providable

import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.model.state.OnlineState
import com.dergoogler.mmrl.platform.content.LocalModule

val LocalRepo = staticCompositionLocalOf<Repo> {
    error("CompositionLocal Repo not present")
}

val LocalOnlineModule = staticCompositionLocalOf<OnlineModule> {
    error("CompositionLocal OnlineModule not present")
}

val LocalOnlineModuleState = staticCompositionLocalOf<OnlineState> {
    error("CompositionLocal OnlineState not present")
}

val LocalModule = staticCompositionLocalOf<LocalModule> {
    error("CompositionLocal LocalModule not present")
}

val LocalVersionItems = staticCompositionLocalOf<List<VersionItem>> {
    error("CompositionLocal LocalVersionItems not present")
}

val LocalVersionItem = staticCompositionLocalOf<VersionItem> {
    error("CompositionLocal LocalVersionItem not present")
}