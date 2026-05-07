package com.dergoogler.mmrl.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.json.UpdateJson
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        application: Application,
        localRepository: LocalRepository,
        modulesRepository: ModulesRepository,
        userPreferencesRepository: UserPreferencesRepository,
    ) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
        private val _updatableModuleCount = MutableStateFlow(0)
        val updatableModuleCount: StateFlow<Int> = _updatableModuleCount

        val versionItemCache = mutableStateMapOf<String, VersionItem?>()

        init {
            refreshUpdatableModules()
        }

        fun refreshUpdatableModules() {
            viewModelScope.launch {
                val modules = localRepository.getLocalAllAsFlow().first()

                val updatableModules =
                    modules.filter {
                        localRepository.hasUpdatableTag(it.id.toString())
                    }

                var count = 0

                for (module in updatableModules) {
                    val id = module.id.toString()

                    val updateVersionItem =
                        if (module.updateJson.isNotBlank()) {
                            UpdateJson.loadToVersionItem(module.updateJson)
                        } else {
                            localRepository.getVersionById(id).firstOrNull()
                        }

                    val installedVersionCode = module.versionCode
                    val updateVersionCode = updateVersionItem?.versionCode ?: -1

                    if (updateVersionCode > installedVersionCode) {
                        count++
                        versionItemCache[id] = updateVersionItem
                    } else {
                        versionItemCache[id] = null
                    }
                }

                _updatableModuleCount.value = count
            }
        }
    }
