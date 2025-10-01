package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.database.entity.Repo.Companion.toRepo
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.datastore.model.Option
import com.dergoogler.mmrl.datastore.model.RepositoryMenu
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.model.state.OnlineState
import com.dergoogler.mmrl.model.state.OnlineState.Companion.createState
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.ext.panicString
import com.dergoogler.mmrl.model.json.UpdateJson
import com.dergoogler.mmrl.model.state.RepoState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = RepositoryViewModel.Factory::class)
class RepositoryViewModel @AssistedInject constructor(
    @Assisted val repo: Repo,
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    private val repositoryMenu
        get() = userPreferencesRepository.data
            .map { it.repositoryMenu }

    var isSearch by mutableStateOf(false)
        private set
    private val keyFlow = MutableStateFlow("")
    val query get() = keyFlow.asStateFlow()

    private val cacheFlow = MutableStateFlow(listOf<Pair<OnlineState, OnlineModule>>())
    private val onlineFlow = MutableStateFlow(listOf<Pair<OnlineState, OnlineModule>>())
    val online get() = onlineFlow.asStateFlow()

    var isLoading by mutableStateOf(true)
        private set

    init {
        Timber.d("RepositoryViewModel init")
        dataObserver()
        keyObserver()
    }

    private fun dataObserver() {
        val onlineModules = if (repo.url.isNotBlank()) {
            localRepository.getOnlineAllByUrlAsFlow(repo.url)
        } else {
            localRepository.getOnlineAllAsFlow()
        }

        combine(
            onlineModules,
            repositoryMenu
        ) { list, menu ->
            cacheFlow.value = list.map {
                it.createState(
                    local = localRepository.getLocalByIdOrNull(it.id),
                    hasUpdatableTag = localRepository.hasUpdatableTag(it.id)
                ) to it
            }.sortedWith(
                comparator(menu.option, menu.descending)
            ).let { v ->
                val a = if (menu.pinInstalled) {
                    v.sortedByDescending { it.first.installed }
                } else {
                    v
                }

                if (menu.pinUpdatable) {
                    a.sortedByDescending { it.first.updatable }
                } else {
                    a
                }
            }

            isLoading = false

        }.launchIn(viewModelScope)
    }

    private fun keyObserver() {
        combine(
            keyFlow,
            cacheFlow
        ) { key, source ->
            val newKey = when {
                key.startsWith("id:", ignoreCase = true) -> key.removePrefix("id:")
                key.startsWith("name:", ignoreCase = true) -> key.removePrefix("name:")
                key.startsWith("author:", ignoreCase = true) -> key.removePrefix("author:")
                key.startsWith("category:", ignoreCase = true) -> key.removePrefix("category:")
                else -> key
            }.trim()

            onlineFlow.value = source.filter { (_, m) ->
                if (key.isNotBlank() || newKey.isNotBlank()) {
                    when {
                        key.startsWith("id:", ignoreCase = true) ->
                            m.id.equals(newKey, ignoreCase = true)

                        key.startsWith("name:", ignoreCase = true) ->
                            m.name.equals(newKey, ignoreCase = true)

                        key.startsWith("author:", ignoreCase = true) ->
                            m.author.equals(newKey, ignoreCase = true)

                        key.startsWith("category:", ignoreCase = true) ->
                            m.categories?.any {
                                it.equals(
                                    newKey,
                                    ignoreCase = true
                                )
                            } ?: false

                        else ->
                            m.name.contains(key, ignoreCase = true) ||
                                    m.author.contains(key, ignoreCase = true) ||
                                    m.description?.contains(key, ignoreCase = true) == true
                    }
                } else {
                    true
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun comparator(
        option: Option,
        descending: Boolean,
    ): Comparator<Pair<OnlineState, OnlineModule>> = if (descending) {
        when (option) {
            Option.Name -> compareByDescending { it.second.name.lowercase() }
            Option.UpdatedTime -> compareBy { it.first.lastUpdated }
            Option.Size -> compareByDescending { 0 }
        }

    } else {
        when (option) {
            Option.Name -> compareBy { it.second.name.lowercase() }
            Option.UpdatedTime -> compareByDescending { it.first.lastUpdated }
            Option.Size -> compareBy { 0 }
        }
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
        fun build(repo: Repo): RepositoryViewModel {
            return hiltViewModel<RepositoryViewModel, Factory> { factory ->
                factory.create(repo)
            }
        }
    }
}