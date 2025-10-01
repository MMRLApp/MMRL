package com.dergoogler.mmrl.ui.screens.search

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.PageIndicator
import com.dergoogler.mmrl.ui.component.scaffold.ResponsiveScaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurSearchToolbar
import com.dergoogler.mmrl.viewmodel.SearchViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun SearchScreen() = LocalScreenProvider {
    val viewModel = hiltViewModel<SearchViewModel>()
    val list by viewModel.online.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    ResponsiveScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BlurSearchToolbar(
                isSearch = true,
                query = query,
                autoFocus = false,
                onQueryChange = viewModel::search,
                title = {},
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        if (viewModel.isLoading) {
            Loading()
        }

        if (list.isEmpty() && !viewModel.isLoading) {
            PageIndicator(
                icon = R.drawable.cloud,
                text = R.string.search_empty,
            )
        }

        ModulesList(
            innerPadding = innerPadding,
            state = listState,
            list = list,
        )
    }
}