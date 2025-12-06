package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.online.Blacklist
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dev.dergoogler.mmrl.compat.model.DialogQueueData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

open class MMRLViewModel
    @Inject
    constructor(
        application: Application,
        val localRepository: LocalRepository,
        val modulesRepository: ModulesRepository,
        val userPreferencesRepository: UserPreferencesRepository,
    ) : AndroidViewModel(application) {
        val isProviderAlive get() = PlatformManager.isAlive
        val platform get() = PlatformManager.platform

        val context
            get(): Context {
                return getApplication<Application>().applicationContext
            }

        internal suspend fun getBlacklistById(id: String?): Blacklist? =
            id?.let { localRepository.getBlacklistByIdOrNullAsFlow(it).first() }

        internal suspend fun localModule(id: String?): LocalModule? = id?.let { localRepository.getLocalByIdOrNullAsFlow(it).first() }

        private val _dialogQueue = MutableStateFlow<List<DialogQueueData>?>(null)
        val currentDialog =
            _dialogQueue.map { it?.firstOrNull() }.stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                null,
            )

        private var onQueueFinished: (() -> Unit)? = null

        fun showDialogs(
            dialogs: List<DialogQueueData>?,
            onFinished: () -> Unit = {},
        ) {
            _dialogQueue.value = dialogs
            onQueueFinished = onFinished
        }

        fun dismissDialog() {
            _dialogQueue.value = _dialogQueue.value?.drop(1)?.takeIf { it.isNotEmpty() }
            if (_dialogQueue.value == null) {
                onQueueFinished?.invoke()
                onQueueFinished = null
            }
        }
    }
