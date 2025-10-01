package com.dergoogler.mmrl.ui.screens.settings.blacklist

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.PageIndicator
import com.dergoogler.mmrl.ui.component.ScaffoldDefaults
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.screens.settings.blacklist.items.ModuleItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.chrisbanes.haze.hazeSource

@Destination<RootGraph>
@Composable
fun BlacklistScreen() {
    val viewModel = LocalSettings.current
    val blacklist = viewModel.allBlacklistEntriesAsFlow

    SettingsScaffold(
        modifier = ScaffoldDefaults.settingsScaffoldModifier,
        title = R.string.settings_blacklist
    ) {
        if (blacklist.isEmpty()) {
            PageIndicator(
                icon = R.drawable.cloud,
                text = R.string.search_empty,
            )
        } else {
            LazyColumn(
                modifier = Modifier.hazeSource(LocalHazeState.current)
            ) {
                items(
                    items = blacklist,
                    key = { it.id }
                ) { module ->
                    ModuleItem(module = module)
                }

                item {
                    val paddingValues = LocalMainScreenInnerPaddings.current
                    Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                }
            }
        }
    }
}