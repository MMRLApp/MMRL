package com.dergoogler.mmrl.viewmodel

import android.app.Application
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.datastore.model.Option
import com.dergoogler.mmrl.datastore.model.RepositoryMenu
import com.dergoogler.mmrl.model.json.UpdateJson
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.model.state.OnlineState
import com.dergoogler.mmrl.model.state.OnlineState.Companion.createState
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

    val listState: LazyListState = LazyListState()

    var isSearch by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(true)
        private set

    private val onlineModules =
        if (repo.url.isNotBlank()) {
            localRepository.getOnlineAllByUrlAsFlow(repo.url)
        } else {
            localRepository.getOnlineAllAsFlow()
        }

    val online: StateFlow<List<Pair<OnlineState, OnlineModule>>> =
        combine(
            onlineModules,
            repositoryMenu,
            keyFlow,
        ) { modules, menu, key ->
            val source = modules.map { module ->
                val local = localRepository.getLocalByIdOrNull(module.id)

                val versions = module.versions.toMutableList()

                if (local != null) {
                    UpdateJson.loadToVersionItem(local.updateJson)?.let {
                        versions.add(0, it)
                    }
                }

                val updatedModule = module.copy(versions = versions)

                updatedModule.createState(
                    local = local,
                    hasUpdatableTag = localRepository.hasUpdatableTag(module.id),
                ) to updatedModule
            }

            val sorted = source
                .sortedWith(comparator(menu.option, menu.descending))
                .let { list ->
                    if (menu.pinInstalled) {
                        list.sortedByDescending { it.first.installed }
                    } else {
                        list
                    }
                }
                .let { list ->
                    if (menu.pinUpdatable) {
                        list.sortedByDescending { it.first.updatable }
                    } else {
                        list
                    }
                }

            isLoading = false

            if (key.isBlank()) {
                sorted
            } else {
                val (searchKey, prefix) = parseSearchKey(key)

                sorted.filter { (_, module) ->
                    when (prefix) {
                        "id" ->
                            module.id.equals(searchKey, ignoreCase = true)

                        "name" ->
                            module.name.equals(searchKey, ignoreCase = true)

                        "author" ->
                            module.author.equals(searchKey, ignoreCase = true)

                        "category" ->
                            module.categories?.any {
                                it.equals(searchKey, ignoreCase = true)
                            } == true

                        else ->
                            module.name.contains(searchKey, ignoreCase = true) ||
                                    module.author.contains(searchKey, ignoreCase = true) ||
                                    module.description?.contains(
                                        searchKey,
                                        ignoreCase = true,
                                    ) == true
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private fun parseSearchKey(raw: String): Pair<String, String?> {
        val prefix = listOf("id", "name", "author", "category")
            .firstOrNull { raw.startsWith("$it:", ignoreCase = true) }

        return if (prefix != null) {
            raw.substringAfter(":").trim() to prefix
        } else {
            raw.trim() to null
        }
    }

    private fun comparator(
        option: Option,
        descending: Boolean,
    ): Comparator<Pair<OnlineState, OnlineModule>> {
        val base: Comparator<Pair<OnlineState, OnlineModule>> =
            when (option) {
                Option.Name ->
                    compareBy { it.second.name.lowercase() }

                Option.UpdatedTime ->
                    compareByDescending { it.first.lastUpdated }

                Option.Size ->
                    compareByDescending { 0 }
            }

        return if (descending) base.reversed() else base
    }

    fun search(key: String) {
        keyFlow.value = key
    }

    fun openSearch() {
        isSearch = true
    }

    fun closeSearch() {
        isSearch = false
        keyFlow.value = ""
    }

    fun setRepositoryMenu(value: RepositoryMenu) {
        viewModelScope.launch {
            userPreferencesRepository.setRepositoryMenu(value)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(repo: Repo): RepositoryViewModel
    }

    companion object {
        @Composable
        fun build(repo: Repo): RepositoryViewModel =
            hiltViewModel<RepositoryViewModel, Factory> { factory ->
                factory.create(repo)
            }
    }
}