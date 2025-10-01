package com.dergoogler.mmrl.ui.screens.repository.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.datastore.model.RepoListMode
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.model.state.OnlineState
import com.dergoogler.mmrl.ui.component.scaffold.ScaffoldScope
import com.dergoogler.mmrl.ui.component.scrollbar.VerticalFastScrollbar
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalOnlineModuleState
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.screens.repository.ModuleItemCompact
import com.dergoogler.mmrl.ui.screens.repository.ModuleItemDetailed
import com.ramcosta.composedestinations.generated.destinations.NewViewScreenDestination
import dev.chrisbanes.haze.hazeSource

@Composable
fun ScaffoldScope.TypedModulesList(
    innerPadding: PaddingValues,
    repo: Repo,
    list: List<Pair<OnlineState, OnlineModule>>,
    state: LazyListState,
) {
    val navigator = LocalDestinationsNavigator.current
    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.repositoryMenu

    val paddingValues = LocalMainScreenInnerPaddings.current
    val layoutDirection = LocalLayoutDirection.current

    val pad = remember(menu) { if (menu.repoListMode == RepoListMode.Compact) 0.dp else 16.dp }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        this@TypedModulesList.ResponsiveContent {
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize().hazeSource(LocalHazeState.current),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + pad,
                    bottom = pad,
                    start = innerPadding.calculateStartPadding(layoutDirection) + pad,
                    end = pad,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = list,
                    key = { it.second.id }
                ) { (moduleState, module) ->
                    CompositionLocalProvider(
                        LocalOnlineModuleState provides moduleState,
                        LocalOnlineModule provides module
                    ) {
                        val click = fun() {
                            navigator.navigate(
                                NewViewScreenDestination(repo, module)
                            )
                        }

                        when (menu.repoListMode) {
                            RepoListMode.Compact -> ModuleItemCompact(
                                onClick = click
                            )

                            RepoListMode.Detailed -> ModuleItemDetailed(
                                onClick = click
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                }
            }
        }

        VerticalFastScrollbar(
            state = state,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        )
    }
}
