package com.dergoogler.mmrl.ui.screens.repository.modules

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.datastore.model.RepositoryMenu
import com.dergoogler.mmrl.ext.isNotNullOrBlank
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.PageIndicator
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurSearchToolbar
import com.dergoogler.mmrl.ui.component.toolbar.ToolbarTitle
import com.dergoogler.mmrl.ui.remember.rememberOnlineModules
import com.dergoogler.mmrl.ui.remember.rememberUserPreferencesRepository
import com.dergoogler.mmrl.ui.screens.repository.RepositoryMenu
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
enum class ModulesFilter {
    ALL, AUTHOR, CATEGORY, NAME
}

@Destination<RootGraph>
@Composable
fun TypedModulesScreen(
    title: String? = null,
    type: ModulesFilter = ModulesFilter.ALL,
    query: String = "",
    repo: Repo,
) = LocalScreenProvider {
    var searchQuery by remember { mutableStateOf("") }
    val modules by rememberOnlineModules(repo, searchQuery)
    val userPreferencesRepository = rememberUserPreferencesRepository()
    val scope = rememberCoroutineScope()
    var isSearch by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    val filterQuery = remember { query }

    val filteredModules = remember(modules, filterQuery, type, searchQuery) {
        modules.filter { (_, m) ->
            val typeMatches = when (type) {
                ModulesFilter.ALL -> true
                ModulesFilter.AUTHOR -> m.author.equals(filterQuery, ignoreCase = true)
                ModulesFilter.CATEGORY -> m.categories?.any {
                    it.equals(
                        filterQuery,
                        ignoreCase = true
                    )
                } ?: false

                ModulesFilter.NAME -> m.name.equals(filterQuery, ignoreCase = true)
            }

            val searchMatches = if (searchQuery.isBlank()) true else {
                m.name.contains(searchQuery, ignoreCase = true) ||
                        m.author.contains(searchQuery, ignoreCase = true) ||
                        (m.categories?.any { it.contains(searchQuery, ignoreCase = true) } ?: false)
            }

            typeMatches && searchMatches
        }
    }

    val openSearch = fun() {
        isSearch = true
    }

    val closeSearch = fun() {
        isSearch = false
        searchQuery = ""
    }

    val search = fun(data: String) { searchQuery = data }

    val setRepositoryMenu = fun(value: RepositoryMenu) {
        scope.launch {
            userPreferencesRepository.setRepositoryMenu(value)
        }
    }

    BackHandler(enabled = isSearch, onBack = closeSearch)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                title = title,
                repo = repo,
                isSearch = isSearch,
                query = searchQuery,
                onQueryChange = search,
                onOpenSearch = openSearch,
                onCloseSearch = closeSearch,
                setMenu = setRepositoryMenu,
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        if (filteredModules.isEmpty()) {
            PageIndicator(
                icon = R.drawable.cloud,
                text = if (isSearch) R.string.search_empty else R.string.repository_empty,
            )
        }

        this@Scaffold.TypedModulesList(
            innerPadding = innerPadding,
            repo = repo,
            list = filteredModules,
            state = listState,
        )
    }
}

@Composable
private fun TopBar(
    title: String?,
    repo: Repo,
    isSearch: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    setMenu: (RepositoryMenu) -> Unit,
) {
    var currentQuery by remember { mutableStateOf(query) }
    DisposableEffect(isSearch) {
        onDispose { currentQuery = "" }
    }

    BlurSearchToolbar(
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
            if (title.isNotNullOrBlank()) {
                ToolbarTitle(
                    title = title,
                    subtitle = repo.name
                )

                return@BlurSearchToolbar
            }

            ToolbarTitle(
                title = repo.name
            )
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

            RepositoryMenu(
                setMenu = setMenu
            )
        }
    )
}