package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.datastore.model.ModulesMenu
import com.dergoogler.mmrl.datastore.model.Option
import com.dergoogler.mmrl.model.json.UpdateJson
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.local.State
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasAction
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasWebUI
import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.service.DownloadService
import com.dergoogler.mmrl.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.File
import javax.inject.Inject

data class ModulesScreenState(
    val items: List<LocalModule> = listOf(),
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class ModulesViewModel
    @Inject
    constructor(
        localRepository: LocalRepository,
        modulesRepository: ModulesRepository,
        userPreferencesRepository: UserPreferencesRepository,
        application: Application,
    ) : MMRLViewModel(
            application = application,
            localRepository = localRepository,
            modulesRepository = modulesRepository,
            userPreferencesRepository = userPreferencesRepository,
        ) {
        val moduleCompatibility: ModuleCompatibility
            get() =
                PlatformManager.get(
                    ModuleCompatibility(
                        hasMagicMount = false,
                        canRestoreModules = false,
                    ),
                ) {
                    with(moduleManager) {
                        moduleCompatibility
                    }
                }

        fun getBlacklist(id: String?) = runBlocking { getBlacklistById(id) }

        private val modulesMenu
            get() =
                userPreferencesRepository.data
                    .map { it.modulesMenu }

        var isSearch by mutableStateOf(false)
            private set
        private val keyFlow = MutableStateFlow("")
        val query get() = keyFlow.asStateFlow()

        val listState: LazyListState = LazyListState()

        private val cacheFlow = MutableStateFlow(listOf<LocalModule>())
        private val localFlow = MutableStateFlow(listOf<LocalModule>())
        val local get() = localFlow.asStateFlow()

        private var isLoadingFlow = MutableStateFlow(false)
        val isLoading get() = isLoadingFlow.asStateFlow()

        private inline fun <T> T.refreshing(callback: T.() -> Unit) {
            isLoadingFlow.update { true }
            callback()
            isLoadingFlow.update { false }
        }

        private val versionItemCache = mutableStateMapOf<ModId, VersionItem?>()

        private val opsTasks = mutableStateListOf<ModId>()
        private val opsCallback =
            object : IModuleOpsCallback.Stub() {
                override fun onSuccess(id: ModId) {
                    viewModelScope.launch {
                        modulesRepository.getLocal(id)
                        opsTasks.remove(id)
                    }
                }

                override fun onFailure(
                    id: ModId,
                    msg: String?,
                ) {
                    opsTasks.remove(id)
                    Timber.w("$id: $msg")
                }
            }

        init {
            Timber.d("ModulesViewModel init")
            providerObserver()
            dataObserver()
            keyObserver()
        }

        private fun providerObserver() {
            PlatformManager.isAliveFlow
                .onEach {
                    if (it) getLocalAll()
                }.launchIn(viewModelScope)
        }

        val screenState: StateFlow<ModulesScreenState> =
            localRepository
                .getLocalAllAsFlow()
                .combine(isLoadingFlow) { items, isRefreshing ->
                    ModulesScreenState(items = items, isRefreshing = isRefreshing)
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = ModulesScreenState(),
                )

        private fun dataObserver() {
            combine(
                localRepository.getLocalAllAsFlow(),
                modulesMenu,
            ) { list, menu ->
                if (list.isEmpty()) return@combine

                cacheFlow.value =
                    list
                        .sortedWith(
                            comparator(menu.option, menu.descending),
                        ).let { v ->
                            val a =
                                if (menu.pinEnabled) {
                                    v.sortedByDescending { it.state == State.ENABLE }
                                } else {
                                    v
                                }

                            val b =
                                if (menu.pinAction) {
                                    a.sortedByDescending { it.hasAction }
                                } else {
                                    a
                                }

                            if (menu.pinWebUI) {
                                b.sortedByDescending { it.hasWebUI }
                            } else {
                                b
                            }
                        }

                isLoadingFlow.update { false }
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
                        else -> key
                    }.trim()

                localFlow.value =
                    source.filter {
                        if (key.isNotBlank() || newKey.isNotBlank()) {
                            when {
                                key.startsWith("id:", ignoreCase = true) ->
                                    it.id.equals(newKey, ignoreCase = true)

                                key.startsWith("name:", ignoreCase = true) ->
                                    it.name.equals(newKey, ignoreCase = true)

                                key.startsWith("author:", ignoreCase = true) ->
                                    it.author.equals(newKey, ignoreCase = true)

                                else ->
                                    it.name.contains(key, ignoreCase = true) ||
                                        it.author.contains(key, ignoreCase = true) ||
                                        it.description.contains(key, ignoreCase = true)
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
        ): Comparator<LocalModule> =
            if (descending) {
                when (option) {
                    Option.Name -> compareByDescending { it.name.lowercase() }
                    Option.UpdatedTime -> compareBy { it.lastUpdated }
                    Option.Size -> compareByDescending { it.size }
                }
            } else {
                when (option) {
                    Option.Name -> compareBy { it.name.lowercase() }
                    Option.UpdatedTime -> compareByDescending { it.lastUpdated }
                    Option.Size -> compareBy { it.size }
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

        fun getLocalAll() =
            viewModelScope.launch {
                refreshing {
                    modulesRepository.getLocalAll()
                }
            }

        fun setModulesMenu(value: ModulesMenu) {
            viewModelScope.launch {
                userPreferencesRepository.setModulesMenu(value)
            }
        }

        fun createModuleOps(
            useShell: Boolean,
            module: LocalModule,
        ) = when (module.state) {
            State.ENABLE ->
                ModuleOps(
                    isOpsRunning = opsTasks.contains(module.id),
                    toggle = {
                        opsTasks.add(module.id)
                        PlatformManager.moduleManager
                            .disable(module.id, useShell, opsCallback)
                    },
                    change = {
                        opsTasks.add(module.id)
                        PlatformManager.moduleManager
                            .remove(module.id, useShell, opsCallback)
                    },
                )

            State.DISABLE ->
                ModuleOps(
                    isOpsRunning = opsTasks.contains(module.id),
                    toggle = {
                        opsTasks.add(module.id)
                        PlatformManager.moduleManager
                            .enable(module.id, useShell, opsCallback)
                    },
                    change = {
                        opsTasks.add(module.id)
                        PlatformManager.moduleManager
                            .remove(module.id, useShell, opsCallback)
                    },
                )

            State.REMOVE ->
                ModuleOps(
                    isOpsRunning = opsTasks.contains(module.id),
                    toggle = {},
                    change = {
                        opsTasks.add(module.id)
                        PlatformManager.moduleManager
                            .enable(module.id, useShell, opsCallback)
                    },
                )

            State.UPDATE ->
                ModuleOps(
                    isOpsRunning = opsTasks.contains(module.id),
                    toggle = {},
                    change = {},
                )
        }

        @Composable
        fun getVersionItem(module: LocalModule): VersionItem? {
            val item by remember {
                derivedStateOf { versionItemCache[module.id] }
            }

            LaunchedEffect(key1 = module) {
                if (!localRepository.hasUpdatableTag(module.id.toString())) {
                    versionItemCache.remove(module.id)
                    return@LaunchedEffect
                }

                if (versionItemCache.containsKey(module.id)) return@LaunchedEffect

                val versionItem =
                    if (module.updateJson.isNotBlank()) {
                        UpdateJson.loadToVersionItem(module.updateJson)
                    } else {
                        localRepository
                            .getVersionById(module.id.toString())
                            .firstOrNull()
                    }

                versionItemCache[module.id] = versionItem
            }

            return item
        }

        fun downloader(
            context: Context,
            module: LocalModule,
            item: VersionItem,
            onSuccess: (File) -> Unit,
        ) {
            viewModelScope.launch {
                val downloadPath =
                    userPreferencesRepository.data
                        .first()
                        .downloadPath

                val filename =
                    Utils.getFilename(
                        name = module.name,
                        version = item.version,
                        versionCode = item.versionCode,
                        extension = "zip",
                    )

                val task =
                    DownloadService.TaskItem(
                        key = item.hashCode(),
                        url = item.zipUrl,
                        filename = filename,
                        title = module.name,
                        desc = item.versionDisplay,
                    )

                val listener =
                    object : DownloadService.IDownloadListener {
                        override fun getProgress(value: Float) {}

                        override fun onFileExists() {
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.file_already_exists),
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }

                        override fun onSuccess() {
                            onSuccess(File(downloadPath).resolve(filename))
                        }

                        override fun onFailure(e: Throwable) {
                            Timber.d(e)
                        }
                    }

                DownloadService.start(
                    context = context,
                    task = task,
                    listener = listener,
                )
            }
        }

        @Composable
        fun getProgress(item: VersionItem?): Float {
            val progress by DownloadService
                .getProgressByKey(item.hashCode())
                .collectAsStateWithLifecycle(initialValue = 0f)

            return progress
        }

        data class ModuleOps(
            val isOpsRunning: Boolean,
            val toggle: (Boolean) -> Unit,
            val change: () -> Unit,
        )
    }
