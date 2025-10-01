package com.dergoogler.mmrl.ui.screens.modules

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.ModulesMenu
import com.dergoogler.mmrl.ext.currentScreenWidth
import com.dergoogler.mmrl.ext.isScrollingUp
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.systemBarsPaddingEnd
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.ui.activity.terminal.install.InstallActivity
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.PageIndicator
import com.dergoogler.mmrl.ui.component.TopAppBarEventIcon
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurSearchToolbar
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.ModulesViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun ModulesScreen(
    viewModel: ModulesViewModel = hiltViewModel(),
) = LocalScreenProvider {
    val userPrefs = LocalUserPreferences.current
    val context = LocalContext.current

    val list by viewModel.local.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    val pullToRefreshState = rememberPullToRefreshState()

    val isScrollingUp by listState.isScrollingUp()
    val showFab by remember {
        derivedStateOf {
            isScrollingUp && !viewModel.isSearch && viewModel.isProviderAlive
        }
    }

    val download: (LocalModule, VersionItem, Boolean) -> Unit = { module, item, install ->
        viewModel.downloader(context, module, item) {
            if (install) {
                InstallActivity.start(
                    context = context,
                    uri = it.toUri()
                )
            }
        }
    }

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
                setMenu = viewModel::setModulesMenu,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn(
                    animationSpec = tween(100),
                    initialScale = 0.8f
                ),
                exit = scaleOut(
                    animationSpec = tween(100),
                    targetScale = 0.8f
                )
            ) {
                FloatingButton()
            }
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
            onRefresh = viewModel::getLocalAll,
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
            this@Scaffold.ModulesList(
                innerPadding = innerPadding,
                list = list,
                state = listState,
                viewModel = viewModel,
                onDownload = download,
                isProviderAlive = viewModel.isProviderAlive,
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
    setMenu: (ModulesMenu) -> Unit,
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

            ModulesMenu(
                setMenu = setMenu
            )
        }
    )
}

@Composable
private fun FloatingButton() {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    val paddingValues = LocalMainScreenInnerPaddings.current

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uri ->
            if (uri.isEmpty()) return@rememberLauncherForActivityResult

            InstallActivity.start(
                context = context,
                uri = uri
            )
        }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                launcher.launch("application/zip")
            }
        }
    }

    FloatingActionButton(
        modifier = Modifier
            .systemBarsPaddingEnd()
            .padding(
                bottom = paddingValues.calculateBottomPadding()
            ),
        interactionSource = interactionSource,
        onClick = {},
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            painter = painterResource(id = R.drawable.package_import),
            contentDescription = null
        )
    }
}