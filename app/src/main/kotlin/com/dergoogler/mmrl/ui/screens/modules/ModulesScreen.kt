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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.ModulesMenu
import com.dergoogler.mmrl.ext.currentScreenWidth
import com.dergoogler.mmrl.ext.isScrollingUp
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.systemBarsPaddingEnd
import com.dergoogler.mmrl.ext.isPackageInstalled
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.local.versionDisplay
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.config
import com.dergoogler.mmrl.ui.activity.terminal.install.InstallActivity
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.PageIndicator
import com.dergoogler.mmrl.ui.component.SearchTopBar
import com.dergoogler.mmrl.ui.component.TopAppBarEventIcon
import com.dergoogler.mmrl.viewmodel.ModulesViewModel
import com.dergoogler.mmrl.utils.WebUIXPackageName
import com.dergoogler.mmrl.utils.toFormattedDateSafely
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toFormattedFileSize

@Composable
fun ModulesScreen(
    viewModel: ModulesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val list by viewModel.local.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    val isScrollingUp by listState.isScrollingUp()
    val showFab by remember {
        derivedStateOf {
            isScrollingUp && !viewModel.isSearch && viewModel.isProviderAlive
        }
    }

    val cachedModuleData = list.associate { module ->
        module.id.toString() to ModuleCacheData(
            displayName = module.config.name ?: module.name,
            versionAuthor = stringResource(
                R.string.module_version_author,
                module.versionDisplay, module.author
            ),
            updateTime = if (module.lastUpdated != 0L) {
                stringResource(
                    R.string.module_update_at,
                    module.lastUpdated.toFormattedDateSafely
                )
            } else null,
            formattedSize = module.size.toFormattedFileSize(),
            isWebUIPackageInstalled = context.isPackageInstalled(WebUIXPackageName)
        )
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
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
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
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::getLocalAll
            ) {
                this@Scaffold.ModulesList(
                    list = list,
                    state = listState,
                    viewModel = viewModel,
                    onDownload = download,
                    cachedModuleData = cachedModuleData
                )
            }
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
) {
    val width = currentScreenWidth()

    var currentQuery by remember { mutableStateOf(query) }
    DisposableEffect(isSearch) {
        onDispose { currentQuery = "" }
    }

    SearchTopBar(
        isSearch = isSearch,
        query = currentQuery,
        onQueryChange = {
            onQueryChange(it)
            currentQuery = it
        },
        onClose = {
            onCloseSearch()
            currentQuery = ""
        },
        title = {
            if (!width.isSmall) return@SearchTopBar

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
        modifier = Modifier.systemBarsPaddingEnd(),
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