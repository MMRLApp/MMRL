package com.dergoogler.mmrl.viewmodel

import android.app.Application
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.datastore.model.RepositoryMenu
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.model.state.OnlineState
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.repository.RepositorySearch
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = RepositoryViewModel.Factory::class)
class RepositoryViewModel
@AssistedInject
constructor(
    @Assisted val repo: Repo,
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(
    application,
    localRepository,
    modulesRepository,
    userPreferencesRepository,
) {
    private val keyFlow = MutableStateFlow("")
    val query: StateFlow<String> = keyFlow.asStateFlow()

    private val repositoryMenu =
        userPreferencesRepository.data.map { it.repositoryMenu }
            .distinctUntilChanged { old, new ->
                old.option == new.option && old.descending == new.descending &&
                    old.pinInstalled == new.pinInstalled && old.pinUpdatable == new.pinUpdatable
            }

    val listState: LazyListState = LazyListState()

    var isSearch by mutableStateOf(false)
        private set

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    class ModulePage(
        val modules: List<Pair<OnlineState, OnlineModule>> = emptyList(),
        val hasMore: Boolean = false,
        val loadedThrough: Int = 0,
    )

    private val requestedCount = MutableStateFlow(PAGE_SIZE)

    @OptIn(ExperimentalCoroutinesApi::class)
    val page: StateFlow<ModulePage> =
        combine(repositoryMenu, keyFlow) { menu, key -> menu to RepositorySearch.parse(key) }
            .flatMapLatest { (menu, search) ->
                localRepository.observeRepositoryChanges().flatMapLatest {
                    val loaded = mutableListOf<Pair<OnlineState, OnlineModule>>()
                    var hasMore = true
                    requestedCount.mapLatest { count ->
                        _isLoading.value = true
                        while (loaded.size < count && hasMore) {
                            val (modules, more) = localRepository.getRepositoryPage(
                                repoUrl = repo.url.takeUnless { it.isBlank() } ?: "",
                                search = search,
                                menu = menu,
                                offset = loaded.size,
                                limit = minOf(PAGE_SIZE, count - loaded.size),
                            )
                            loaded.addAll(modules)
                            hasMore = more
                        }
                        _isLoading.value = false
                        ModulePage(
                            modules = loaded.take(count),
                            hasMore = hasMore || loaded.size > count,
                            loadedThrough = minOf(count, loaded.size),
                        )
                    }
                }
            }
            .onStart { _isLoading.value = true }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ModulePage(),
            )

    fun search(key: String) {
        if (keyFlow.value == key) return
        _isLoading.value = true
        requestedCount.value = PAGE_SIZE
        keyFlow.value = key
        viewModelScope.launch { listState.scrollToItem(0) }
    }

    fun loadMore() {
        val current = page.value
        if (!_isLoading.value && current.hasMore && requestedCount.value <= current.loadedThrough) {
            _isLoading.value = true
            requestedCount.value += PAGE_SIZE
        }
    }

    fun openSearch() {
        isSearch = true
    }

    fun closeSearch() {
        isSearch = false
        search("")
    }

    fun setRepositoryMenu(value: RepositoryMenu) {
        viewModelScope.launch {
            val current = repositoryMenu.first()
            if (current.option != value.option || current.descending != value.descending ||
                current.pinInstalled != value.pinInstalled || current.pinUpdatable != value.pinUpdatable
            ) {
                _isLoading.value = true
                requestedCount.value = PAGE_SIZE
                listState.scrollToItem(0)
            }
            userPreferencesRepository.setRepositoryMenu(value)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(repo: Repo): RepositoryViewModel
    }

    companion object {
        private const val PAGE_SIZE = 30
        @Composable
        fun build(repo: Repo): RepositoryViewModel =
            androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<RepositoryViewModel, Factory> { factory ->
                factory.create(repo)
            }

        private val local =
            staticCompositionLocalOf<RepositoryViewModel> { error("No RepositoryViewModel found!") }

        @get:Composable
        val current: RepositoryViewModel get() = local.current

        @Composable
        fun LocalProvider(repo: Repo, content: @Composable (RepositoryViewModel) -> Unit) {
            val vm = build(repo)
            CompositionLocalProvider(
                local provides vm,
            ) {
                content(vm)
            }
        }
    }
}
