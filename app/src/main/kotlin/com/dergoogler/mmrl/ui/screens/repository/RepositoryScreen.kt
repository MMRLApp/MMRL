package com.dergoogler.mmrl.ui.screens.repository

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.isNotNullOrBlank
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.nullply
import com.dergoogler.mmrl.ext.onClick
import com.dergoogler.mmrl.ext.stripLinks
import com.dergoogler.mmrl.model.ui.TopCategory
import com.dergoogler.mmrl.ui.component.Cover
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.PageIndicator
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.text.BBCodeText
import com.dergoogler.mmrl.ui.component.toolbar.BlurToolbar
import com.dergoogler.mmrl.ui.component.toolbar.ToolbarTitle
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalRepo
import com.dergoogler.mmrl.ui.screens.repository.modules.ModulesFilter
import com.dergoogler.mmrl.viewmodel.RepositoryViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.TypedModulesScreenDestination
import dev.chrisbanes.haze.hazeSource
import dev.dergoogler.mmrl.compat.core.LocalUriHandler

@Destination<RootGraph>()
@Composable
fun RepositoryScreen(repo: Repo) = LocalScreenProvider {
    val viewModel = RepositoryViewModel.build(repo)
    val list by viewModel.online.collectAsStateWithLifecycle()

    val browser = LocalUriHandler.current
    val density = LocalDensity.current
    val navigator = LocalDestinationsNavigator.current
    val hazeState = LocalHazeState.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    BackHandler(
        enabled = viewModel.isSearch,
        onBack = viewModel::closeSearch
    )

    val modules = remember(list) { list.map { it.second } }
    val topCategories = remember(modules) { TopCategory.fromModuleList(modules) }

    var coverHeight by remember { mutableIntStateOf(0) }
    var cardHeight by remember { mutableIntStateOf(0) }

    CompositionLocalProvider(
        LocalRepo provides repo
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                BlurToolbar(
                    fadeBackgroundIfNoBlur = true,
                    navigationIcon = {
                        IconButton(onClick = { navigator.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_left),
                                contentDescription = null
                            )
                        }
                    },
                    title = {
                        ToolbarTitle(
                            modifier = Modifier.alpha(it),
                            title = repo.name
                        )
                    },
                    fade = true,
                    scrollBehavior = scrollBehavior
                )
            },
            contentWindowInsets = WindowInsets.none
        ) { _ ->
            ResponsiveContent {
                if (viewModel.isLoading) {
                    Loading()

                    return@ResponsiveContent
                }

                if (list.isEmpty() && !viewModel.isLoading) {
                    PageIndicator(
                        icon = R.drawable.cloud,
                        text = if (viewModel.isSearch) R.string.search_empty else R.string.repository_empty,
                    )

                    return@ResponsiveContent
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Box {
                            Cover(
                                modifier = Modifier
                                    .fadingEdge(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black),
                                            startY = Float.POSITIVE_INFINITY,
                                            endY = 0f
                                        )
                                    )
                                    .onGloballyPositioned { coordinates ->
                                        coverHeight = coordinates.size.height
                                    },
                                url = repo.cover ?: "ERROR ME",
                                errorIcon = R.drawable.box,
                            )

                            Box(
                                modifier = Modifier
                                    .offset(y = 164.4.dp)
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth(0.85f)
                                    .onGloballyPositioned { coordinates ->
                                        cardHeight = coordinates.size.height
                                    }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                        1.dp
                                                    ),
                                                    MaterialTheme.colorScheme.background
                                                ),
                                                start = Offset(0f, 0f),
                                                end = Offset(
                                                    Float.POSITIVE_INFINITY,
                                                    Float.POSITIVE_INFINITY
                                                )
                                            ),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .border(
                                            Dp.Hairline,
                                            MaterialTheme.colorScheme.outlineVariant,
                                            RoundedCornerShape(20.dp)
                                        ),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .relative()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                            text = (repo.description
                                                ?: stringResource(R.string.view_module_no_description)).stripLinks(
                                                "<URL>"
                                            ),
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            lineHeight = 20.sp
                                        )

                                        FlowRow {
                                            repo.nullply {
                                                if (submission.isNotNullOrBlank()) {
                                                    LabelItem(
                                                        style = LabelItemDefaults.style.copy(
                                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                        ),
                                                        modifier = Modifier.onClick {
                                                            browser.openUri(submission)
                                                        },
                                                        icon = getDomainIcon(submission),
                                                        text = stringResource(R.string.repo_options_submission)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            with(density) {
                                Spacer(modifier = Modifier.height(((coverHeight / 1.2f) + cardHeight).toDp()))
                            }
                        }
                    }

                    item {
                        val takenModules = remember { modules.take(9) }
                        val title = stringResource(R.string.page_modules)

                        TopPicks(
                            label = title,
                            list = takenModules,
                            onMoreClick = {
                                navigator.navigate(
                                    TypedModulesScreenDestination(
                                        type = ModulesFilter.ALL,
                                        title = title,
                                        repo = repo,
                                    )
                                )
                            }
                        )
                    }

                    items(
                        items = topCategories,
                        key = { it.label }
                    ) {
                        TopPicks(
                            label = it.label,
                            list = it.modules,
                            onMoreClick = {
                                navigator.navigate(
                                    TypedModulesScreenDestination(
                                        type = ModulesFilter.CATEGORY,
                                        title = it.label,
                                        repo = repo,
                                        query = it.label
                                    )
                                )
                            }
                        )
                    }

                    item {
                        Spacer(
                            Modifier.height(16.dp)
                        )

                        val paddingValues = LocalMainScreenInnerPaddings.current
                        Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                    }
                }
            }
        }
    }
}

@Composable
fun getDomainIcon(url: String) = when (getDomain(url)) {
    "github.com" -> R.drawable.github
    else -> R.drawable.link
}

fun getDomain(url: String): String {
    return try {
        val uri = url.toUri()
        val host = uri.host ?: ""
        // Remove "www." if present
        if (host.startsWith("www.")) host.substring(4) else host
    } catch (_: Exception) {
        ""
    }
}