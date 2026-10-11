package com.dergoogler.mmrl.ui.screens.repository

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.datastore.model.RepoListMode
import com.dergoogler.mmrl.datastore.model.RepositoryMenu
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.isNotNullOrBlank
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.nullply
import com.dergoogler.mmrl.ext.onClick
import com.dergoogler.mmrl.ext.stripLinks
import com.dergoogler.mmrl.ui.component.Cover
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.PageIndicator
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.text.BBCodeText
import com.dergoogler.mmrl.ui.component.toolbar.BlurSearchToolbar
import com.dergoogler.mmrl.ui.component.toolbar.ToolbarTitle
import com.dergoogler.mmrl.ui.component.toolbar.rememberBlurToolbarState
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.RepositoryViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.dergoogler.mmrl.compat.core.LocalUriHandler

@Destination<RootGraph>
@Composable
fun RepositoryScreen(
    repo: Repo,
) = LocalScreenProvider {
    val userPreferences = LocalUserPreferences.current
    val browser = LocalUriHandler.current
    val density = LocalDensity.current

    val viewModel = RepositoryViewModel.build(repo)
    val modules by viewModel.online.collectAsStateWithLifecycle()

    val query by viewModel.query.collectAsStateWithLifecycle()
    val openSearch = viewModel::openSearch
    val closeSearch = viewModel::closeSearch
    val search = viewModel::search
    val isSearch by viewModel::isSearch
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var coverHeight by remember { mutableIntStateOf(0) }
    var cardHeight by remember { mutableIntStateOf(0) }

    val menu = userPreferences.repositoryMenu
    val pad = remember(menu) { if (menu.repoListMode == RepoListMode.Compact) 0.dp else 16.dp }

    BackHandler(enabled = viewModel.isSearch, onBack = viewModel::closeSearch)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar0(
                repo = repo,
                isSearch = isSearch,
                query = query,
                onQueryChange = search,
                onOpenSearch = openSearch,
                onCloseSearch = closeSearch,
                scrollBehavior = scrollBehavior,
                setMenu = viewModel::setRepositoryMenu,
            )
        },
        contentWindowInsets = WindowInsets.none,
    ) { innerPadding ->
        if (modules.isEmpty()) {
            PageIndicator(
                icon = R.drawable.cloud,
                text = if (isSearch) R.string.search_empty else R.string.repository_empty,
            )

            return@Scaffold
        }

        this@Scaffold.ModulesList(
            innerPadding = innerPadding,
            repo = repo,
            list = modules,
            state = viewModel.listState,
        ) {
            item {
                AnimatedVisibility(isSearch) {
                    Spacer(Modifier.height(innerPadding.calculateTopPadding() + pad))
                }

                AnimatedVisibility(!isSearch) {
                    Box(Modifier.animateEnterExit()) {
                        Cover(
                            modifier =
                                Modifier
                                    .fadingEdge(
                                        brush =
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black
                                                ),
                                                startY = Float.POSITIVE_INFINITY,
                                                endY = 0f,
                                            ),
                                    )
                                    .onGloballyPositioned { coordinates ->
                                        coverHeight = coordinates.size.height
                                    },
                            url = repo.cover ?: "ERROR ME",
                            errorIcon = R.drawable.box,
                        )

                        Box(
                            modifier =
                                Modifier
                                    .offset(y = 164.4.dp)
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth(0.85f)
                                    .onGloballyPositioned { coordinates ->
                                        cardHeight = coordinates.size.height
                                    },
                        ) {
                            Card(
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            brush =
                                                Brush.linearGradient(
                                                    colors =
                                                        listOf(
                                                            MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                                1.dp,
                                                            ),
                                                            MaterialTheme.colorScheme.background,
                                                        ),
                                                    start = Offset(0f, 0f),
                                                    end =
                                                        Offset(
                                                            Float.POSITIVE_INFINITY,
                                                            Float.POSITIVE_INFINITY,
                                                        ),
                                                ),
                                            RoundedCornerShape(20.dp),
                                        )
                                        .border(
                                            Dp.Hairline,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(20.dp),
                                        ),
                            ) {
                                Column(
                                    modifier =
                                        Modifier
                                            .relative()
                                            .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    Text(
                                        text = repo.name,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )

                                    BBCodeText(
                                        text =
                                            (
                                                    repo.description
                                                        ?: stringResource(R.string.view_module_no_description)
                                                    ).stripLinks(
                                                    "<URL>",
                                                ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        lineHeight = 20.sp,
                                    )

                                    FlowRow {
                                        repo.nullply {
                                            if (submission.isNotNullOrBlank()) {
                                                LabelItem(
                                                    style =
                                                        LabelItemDefaults.style.copy(
                                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        ),
                                                    modifier =
                                                        Modifier.onClick {
                                                            browser.openUri(submission)
                                                        },
                                                    icon = getDomainIcon(submission),
                                                    text = stringResource(R.string.repo_options_submission),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        with(density) {
                            Spacer(modifier = Modifier.height((((coverHeight / 1.05f) + cardHeight)).toDp()))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar0(
    repo: Repo,
    isSearch: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    setMenu: (RepositoryMenu) -> Unit,
) {
    val state = rememberBlurToolbarState(scrollBehavior, fade = true)

    var currentQuery by remember { mutableStateOf(query) }
    DisposableEffect(isSearch) {
        onDispose { currentQuery = "" }
    }

    BlurSearchToolbar(
        state = state,
        fadeBackgroundIfNoBlur = true,
        isSearch = isSearch,
        query = currentQuery,
        scrollBehavior = scrollBehavior,
        fade = true,
        fadeDistance = 200f,
        onQueryChange = {
            onQueryChange(it)
            currentQuery = it
        },
        onClose = {
            onCloseSearch()
            currentQuery = ""
        },
        title = {
            ToolbarTitle(
                title = repo.name,
                modifier = Modifier.graphicsLayer { alpha = state.alpha },
            )
        },
        actions = {
            if (!isSearch) {
                IconButton(
                    onClick = onOpenSearch,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = null,
                    )
                }
            }

            RepositoryMenu(
                setMenu = setMenu,
            )
        },
    )
}

@Composable
fun getDomainIcon(url: String) =
    when (getDomain(url)) {
        "github.com" -> R.drawable.github
        else -> R.drawable.link
    }

fun getDomain(url: String): String =
    try {
        val uri = url.toUri()
        val host = uri.host ?: ""
        // Remove "www." if present
        if (host.startsWith("www.")) host.substring(4) else host
    } catch (_: Exception) {
        ""
    }