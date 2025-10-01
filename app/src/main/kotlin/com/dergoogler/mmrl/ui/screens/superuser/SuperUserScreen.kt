package com.dergoogler.mmrl.ui.screens.superuser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.SuperUserMenu
import com.dergoogler.mmrl.ext.currentScreenWidth
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.PageIndicator
import com.dergoogler.mmrl.ui.component.TopAppBarEventIcon
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurSearchToolbar
import com.dergoogler.mmrl.ui.providable.LocalSuperUserViewModel
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun SuperUserScreen() = LocalScreenProvider {
    val userPrefs = LocalUserPreferences.current
    val viewModel = LocalSuperUserViewModel.current

    val list by viewModel.local.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    val pullToRefreshState = rememberPullToRefreshState()

    BackHandler(
        enabled = viewModel.isSearch,
        onBack = viewModel::closeSearch
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                isSearch = viewModel.isSearch,
                query = query,
                onQueryChange = viewModel::search,
                onOpenSearch = viewModel::openSearch,
                onCloseSearch = viewModel::closeSearch,
                setMenu = viewModel::setSuperUserMenu,
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        if (isLoading) {
            Loading()
        }

        if (list.isEmpty() && !isLoading) {
            PageIndicator(
                icon = R.drawable.keyframes,
                text = if (viewModel.isSearch) R.string.search_empty else R.string.modules_empty,
            )
        }

        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::fetchAppList,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter).let {
                        if (!userPrefs.enableBlur) {
                            it.padding(top = innerPadding.calculateTopPadding())
                        } else it
                    },
                    isRefreshing = state.isRefreshing,
                    state = pullToRefreshState
                )
            }
        ) {
            this@Scaffold.SuperUserList(
                innerPadding = innerPadding,
                list = list,
                state = listState,
            )
        }
    }
}

@Composable
private fun TopBar(
    isSearch: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    setMenu: (SuperUserMenu) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val width = currentScreenWidth()

    var currentQuery by remember { mutableStateOf(query) }
    DisposableEffect(isSearch) {
        onDispose { currentQuery = "" }
    }

    BlurSearchToolbar(
        scrollBehavior = scrollBehavior,
        isSearch = isSearch,
        query = currentQuery,
        fade = true,
        fadeDistance = 50f,
        onQueryChange = {
            onQueryChange(it)
            currentQuery = it
        },
        onClose = {
            onCloseSearch()
            currentQuery = ""
        },
        title = {
            if (!width.isSmall) return@BlurSearchToolbar

            TopAppBarEventIcon()
        },
        actions = {
            if (!isSearch) {
                IconButton(
                    onClick = onOpenSearch
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = null
                    )
                }
            }

            SuperUserMenu(
                setMenu = setMenu
            )
        }
    )
}
