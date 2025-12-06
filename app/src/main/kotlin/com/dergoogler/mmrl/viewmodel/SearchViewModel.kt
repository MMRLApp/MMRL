package com.dergoogler.mmrl.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.online.OtherSources
import com.dergoogler.mmrl.model.state.OnlineState.Companion.createState
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        application: Application,
        localRepository: LocalRepository,
        modulesRepository: ModulesRepository,
        userPreferencesRepository: UserPreferencesRepository,
    ) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
        private val keyFlow = MutableStateFlow("")
        val query get() = keyFlow.asStateFlow()

        private val cacheFlow = MutableStateFlow(listOf<OtherSources>())
        private val onlineFlow = MutableStateFlow(listOf<OtherSources>())
        val online get() = onlineFlow.asStateFlow()

        var isLoading by mutableStateOf(true)
            private set

        init {
            dataObserver()
            keyObserver()
        }

        private fun dataObserver() {
            val onlineModules = localRepository.getOnlineAllAsFlow(duplicates = true)

            combine(
                onlineModules,
            ) { list ->
                cacheFlow.value =
                    list.first().map {
                        OtherSources(
                            repo = localRepository.getRepoByUrl(it.repoUrl),
                            online = it,
                            state =
                                it.createState(
                                    local = localRepository.getLocalByIdOrNull(it.id),
                                    hasUpdatableTag = localRepository.hasUpdatableTag(it.id),
                                ),
                        )
                    }

                isLoading = false
            }.launchIn(viewModelScope)
        }

        private fun keyObserver() {
            combine(
                keyFlow,
                cacheFlow,
            ) { key, source ->
                val newKey =
                    when {
                        key.startsWith("id:", ignoreCase = true) -> key.removePrefix("id:")
                        key.startsWith("name:", ignoreCase = true) -> key.removePrefix("name:")
                        key.startsWith("author:", ignoreCase = true) -> key.removePrefix("author:")
                        key.startsWith("category:", ignoreCase = true) -> key.removePrefix("category:")
                        else -> key
                    }.trim()

                onlineFlow.value =
                    source.filter { (_, m) ->
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
                                            ignoreCase = true,
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

        fun search(key: String) {
            keyFlow.value = key
        }
    }
