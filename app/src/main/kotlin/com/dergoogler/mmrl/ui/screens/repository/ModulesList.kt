package com.dergoogler.mmrl.ui.screens.repository

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.datastore.model.RepoListMode
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.model.state.OnlineState
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalOnlineModuleState
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.generated.destinations.NewViewScreenDestination
import dev.mmrlx.compose.ui.scaffold.ScaffoldScope

@Composable
fun ScaffoldScope.ModulesList(
    repo: Repo,
    list: List<Pair<OnlineState, OnlineModule>>,
    state: LazyListState,
    beforeContent: LazyListScope.() -> Unit = {},
) {
    val navigator = LocalDestinationsNavigator.current
    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.repositoryMenu

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .animateContentSize(),
            contentPadding =
                PaddingValues(
                    bottom = this@ModulesList.scaffoldBottomPadding + 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            beforeContent()

            items(
                items = list,
                key = { it.second.id },
            ) { (moduleState, module) ->
                CompositionLocalProvider(
                    LocalOnlineModuleState provides moduleState,
                    LocalOnlineModule provides module,
                ) {
                    val click = fun() {
                        navigator.navigate(
                            NewViewScreenDestination(repo, module),
                        )
                    }

                    Box(Modifier.padding(horizontal = 16.dp)) {
                        when (menu.repoListMode) {
                            RepoListMode.Compact ->
                                ModuleItemCompact(
                                    onClick = click,
                                )

                            RepoListMode.Detailed ->
                                ModuleItemDetailed(
                                    onClick = click,
                                )
                        }
                    }
                }
            }
        }
    }
}