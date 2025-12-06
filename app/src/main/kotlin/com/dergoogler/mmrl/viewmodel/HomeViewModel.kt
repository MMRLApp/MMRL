package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.Context
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.local.ModuleAnalytics
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.content.NullableBoolean
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        application: Application,
        localRepository: LocalRepository,
        modulesRepository: ModulesRepository,
        userPreferencesRepository: UserPreferencesRepository,
    ) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
        val versionName: String
            get() =
                PlatformManager.get("") {
                    with(moduleManager) { version }
                }

        val isLkmMode: NullableBoolean
            get() =
                PlatformManager.get(NullableBoolean(null)) {
                    with(moduleManager) { isLkmMode }
                }

        val versionCode
            get() =
                PlatformManager.get(0) {
                    with(moduleManager) { versionCode }
                }

        val seLinuxContext: String
            get() =
                PlatformManager.get("Failed") {
                    seLinuxContext
                }

        val superUserCount: Int
            get() =
                PlatformManager.get(-1) {
                    with(moduleManager) {
                        superUserCount
                    }
                }

        fun analytics(context: Context): ModuleAnalytics? =
            PlatformManager.get(null) {
                with(moduleManager) {
                    val local = runBlocking { localRepository.getLocalAllAsFlow().first() }
                    return@get ModuleAnalytics(
                        context = context,
                        local = local,
                    )
                }
            }

        fun reboot(reason: String = "") {
            PlatformManager.get(Unit) {
                with(moduleManager) {
                    reboot(reason)
                }
            }
        }
    }
