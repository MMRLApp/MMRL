package com.dergoogler.mmrl.ui.screens.exploreRepositories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.model.online.ExploreRepository
import com.dergoogler.mmrl.network.runRequest
import com.dergoogler.mmrl.stub.IMMRLApiManager
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurNavigateUpToolbar
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.screens.exploreRepositories.items.HeadlineCard
import com.dergoogler.mmrl.ui.screens.exploreRepositories.items.RepoCard
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Destination<RootGraph>
@Composable
fun ExploreRepositoriesScreen() = LocalScreenProvider {
    var exploreRepositories by remember { mutableStateOf<List<ExploreRepository>?>(null) }

    val navigator = LocalDestinationsNavigator.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        runRequest {
            withContext(Dispatchers.IO) {
                val api = IMMRLApiManager.build()
                return@withContext api.repositories.execute()
            }
        }.onSuccess { list ->
            exploreRepositories = list
        }.onFailure {
            Timber.e(it, "unable to get recommended repos")
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BlurNavigateUpToolbar(
                title = stringResource(id = R.string.explore_repositories),
                fade = true,
                fadeDistance = 50f
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        this@Scaffold.ResponsiveContent {
            AnimatedVisibility(
                visible = exploreRepositories == null, enter = fadeIn(), exit = fadeOut()
            ) {
                Loading()
            }

            AnimatedVisibility(
                visible = exploreRepositories != null, enter = fadeIn(), exit = fadeOut()
            ) {
                exploreRepositories.nullable { er ->
                    LazyColumn(
                        modifier = Modifier.hazeSource(LocalHazeState.current),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = innerPadding.calculateTopPadding() + 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                        )
                    ) {
                        item {
                            HeadlineCard(
                                repoCount = er.size,
                                moduleCount = er.sumOf { it.modulesCount ?: 0 }
                            )
                        }

                        items(
                            items = er,
                            key = { it.url }
                        ) { repo ->
                            RepoCard(
                                repo = repo
                            )
                        }

                        item {
                            val paddingValues = LocalMainScreenInnerPaddings.current
                            Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                        }
                    }
                }
            }
        }
    }
}