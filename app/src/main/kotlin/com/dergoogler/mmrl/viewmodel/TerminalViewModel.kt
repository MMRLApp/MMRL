package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.utils.initPlatform
import dev.mmrlx.terminal.TerminalEmulator
import dev.mmrlx.terminal.appendLineOnMain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

open class TerminalViewModel
@Inject
constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    protected val logs = mutableListOf<String>()

    protected var emulator: TerminalEmulator? = null
    protected val emulatorReady = CompletableDeferred<TerminalEmulator>()

    var event by mutableStateOf(Event.LOADING)

    val terminal by mutableStateOf<TerminalEmulator?>(null)

    private val localFlow = MutableStateFlow<LocalModule?>(null)
    val local get() = localFlow.asStateFlow()

    protected val platformReadyDeferred = CompletableDeferred<Boolean>()

    init {
        viewModelScope.launch {
            val userPreferences = userPreferencesRepository.data.first()

            devLog(R.string.waiting_for_platformmanager_to_initialize)

            val deferred =
                initPlatform(
                    scope = viewModelScope,
                    context = context,
                    platform = userPreferences.workingMode.toPlatform(),
                )

            val platformInitializedSuccessfully = deferred.await()
            if (!platformInitializedSuccessfully) {
                event = Event.FAILED
                log(R.string.failed_to_initialize_platform)
                platformReadyDeferred.complete(false)
            } else {
                devLog(R.string.platform_initialized)
                platformReadyDeferred.complete(true)
            }
        }
    }

    override fun onCleared() {
//        terminal.shell.close()
//        terminal.currentCard = null
//        terminal.currentGroup = null
        super.onCleared()
    }

    fun reboot(reason: String = "") {
        PlatformManager.moduleManager.reboot(reason)
    }

    fun onEmulatorCreated(emu: TerminalEmulator) {
        emulator = emu
        emulatorReady.complete(emu)
    }

    suspend fun writeLogsTo(uri: Uri) =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(logs.joinToString(separator = "\n").toByteArray())
                }
            }.onFailure {
                Timber.e(it)
            }
        }

    private val localizedEnglishResources
        get(): Resources {
            var conf: Configuration = context.resources.configuration
            conf = Configuration(conf)
            conf.setLocale(Locale.ENGLISH)
            val localizedContext = context.createConfigurationContext(conf)
            return localizedContext.resources
        }

    private val devMode = runBlocking { userPreferencesRepository.data.first().developerMode }

    @MainThread
    protected fun devLog(message: String) {
        if (devMode) log(message)
    }

    @MainThread
    protected fun devLog(
        @StringRes message: Int,
        vararg format: Any?,
    ) {
        if (devMode) log(message, *format)
    }

    @MainThread
    protected fun devLog(
        @StringRes message: Int,
    ) {
        if (devMode) log(message)
    }

    @MainThread
    protected fun log(
        @StringRes message: Int,
        vararg format: Any?,
    ) {
        log(
            message = context.getString(message, *format),
            log = localizedEnglishResources.getString(message, *format),
        )
    }

    @MainThread
    protected fun log(
        @StringRes message: Int,
    ) {
        log(
            message = context.getString(message),
            log = localizedEnglishResources.getString(message),
        )
    }

    @MainThread
    protected fun log(
        message: String,
        log: String = message,
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            val emu = emulatorReady.await()
            emu.appendLineOnMain(message)
            logs += log

        }
    }
}
