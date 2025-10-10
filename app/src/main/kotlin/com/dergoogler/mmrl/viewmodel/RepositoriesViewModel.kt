package com.dergoogler.mmrl.viewmodel

import android.app.Application
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.database.entity.Repo.Companion.toRepo
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.datastore.model.Option
import com.dergoogler.mmrl.datastore.model.RepositoriesMenu
import com.dergoogler.mmrl.model.state.RepoState
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class RepositoriesScreenState(
    val items: List<RepoState> = listOf(),
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class RepositoriesViewModel @Inject constructor(
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
    application: Application,
) : MMRLViewModel(
    application = application,
    localRepository = localRepository,
    modulesRepository = modulesRepository,
    userPreferencesRepository = userPreferencesRepository
) {
    private val reposFlow = MutableStateFlow(listOf<RepoState>())
    val repos get() = reposFlow.asStateFlow()

    val listState: LazyListState = LazyListState()

    private val repositoriesMenu
        get() = userPreferencesRepository.data
            .map { it.repositoriesMenu }

    var isLoading by mutableStateOf(true)
        private set
    private var progressFlow = MutableStateFlow(false)
    val progress get() = progressFlow.asStateFlow()

    private inline fun <T> T.refreshing(callback: T.() -> Unit) {
        progressFlow.update { true }
        callback()
        progressFlow.update { false }
    }

    init {
        Timber.d("CustomRepositoriesViewModel init")
        dataObserver()
    }

    private fun dataObserver() {

        combine(
            localRepository.getRepoAllAsFlow(),
            repositoriesMenu
        ) { list, menu ->
            reposFlow.value = list.map {
                RepoState(it)
            }.sortedWith(
                comparator(menu.option, menu.descending)
            )

            isLoading = false

        }.launchIn(viewModelScope)
    }

    private fun comparator(
        option: Option,
        descending: Boolean,
    ): Comparator<RepoState> = if (descending) {
        when (option) {
            Option.Name -> compareByDescending { it.name.lowercase() }
            Option.UpdatedTime -> compareBy { it.timestamp }
            Option.Size -> compareByDescending { it.size }
        }

    } else {
        when (option) {
            Option.Name -> compareBy { it.name.lowercase() }
            Option.UpdatedTime -> compareByDescending { it.timestamp }
            Option.Size -> compareBy { it.size }
        }
    }

    fun setRepositoriesMenu(value: RepositoriesMenu) {
        viewModelScope.launch {
            userPreferencesRepository.setRepositoriesMenu(value)
        }
    }

    val screenState: StateFlow<ModulesScreenState> = localRepository.getLocalAllAsFlow()
        .combine(progress) { items, isRefreshing ->
            ModulesScreenState(items = items, isRefreshing = isRefreshing)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ModulesScreenState()
        )

    fun insert(
        url: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: (Throwable) -> Unit,
    ) = viewModelScope.launch {
        refreshing {
            modulesRepository.getRepo(url.toRepo()).apply {
                onFailure(onFailure)
                onSuccess?.let {
                    onSuccess {
                        it()
                    }
                }
            }
        }
    }

    fun update(repo: RepoState) = viewModelScope.launch {
        localRepository.insertRepo(repo.toRepo())
    }

    fun delete(repo: RepoState) = viewModelScope.launch {
        localRepository.deleteRepo(repo.toRepo())
        localRepository.deleteOnlineByUrl(repo.url)
    }

    fun getUpdate(
        repo: RepoState,
        onFailure: (Throwable) -> Unit,
    ) = viewModelScope.launch {
        refreshing {
            modulesRepository.getRepo(repo.toRepo())
                .onFailure(onFailure)
        }
    }

    fun getRepoAll() = viewModelScope.launch {
        refreshing {
            modulesRepository.getRepoAll(onlyEnable = false)
        }
    }
}