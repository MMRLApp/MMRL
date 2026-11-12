package com.dergoogler.mmrl.ui.screens.repository

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.model.state.OnlineState
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalOnlineModuleState
import com.dergoogler.mmrl.viewmodel.RepositoryViewModel
import com.ramcosta.composedestinations.generated.destinations.NewViewScreenDestination
import dev.mmrlx.compose.ui.scaffold.ScaffoldScope
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ScaffoldScope.ModulesList(
    repo: Repo,
    list: List<Pair<OnlineState, OnlineModule>>,
    state: LazyListState,
    hasMore: Boolean,
    isLoading: Boolean,
    beforeContent: LazyListScope.() -> Unit = {},
) {
    val scope = this
    val vm = RepositoryViewModel.current
    val navigator = LocalDestinationsNavigator.current
    val isSearch by vm::isSearch

    LaunchedEffect(state, list.size, hasMore, isLoading) {
        if (list.isNotEmpty() && hasMore && !isLoading) {
            snapshotFlow {
                val layout = state.layoutInfo
                val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
                lastVisible >= 0 && lastVisible >= layout.totalItemsCount - 4
            }.distinctUntilChanged().collect { nearEnd ->
                if (nearEnd) vm.loadMore()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .animateContentSize(),
            contentPadding =
                PaddingValues(
                    top = if (!isSearch) 0.dp else scope.scaffoldTopPadding,
                    bottom = scope.scaffoldBottomPadding + 8.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    Box(
                        modifier = Modifier.padding(
                            start = scope.scaffoldStartPadding + 16.dp,
                            end = scope.scaffoldEndPadding + 16.dp
                        )
                    ) {
                        ModuleItemDetailed(
                            onClick = {
                                navigator.navigate(
                                    NewViewScreenDestination(repo, module),
                                )
                            },
                        )
                    }
                }
            }
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}