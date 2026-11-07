package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
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
import dev.mmrlx.compose.ui.theme.Colors
import dev.mmrlx.terminal.Pty
import dev.mmrlx.terminal.TerminalEmulator
import dev.mmrlx.terminal.appendLineOnMain
import dev.mmrlx.terminal.appendTextOnMain
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

open class TerminalViewModel(
    val mxColors: Colors,
    val mdColors: ColorScheme,
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    protected val logs = mutableListOf<String>()

    protected val emulatorDeferred = CompletableDeferred<TerminalEmulator>()
    protected var ptyDeferred = CompletableDeferred<Pty>()

    var event by mutableStateOf(Event.LOADING)


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

    fun closePty() {
        viewModelScope.launch {
            val pty = ptyDeferred.await()
            pty.close()
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            val pty = ptyDeferred.await()

            try {
                pty.close()
                pty.destroy()
            } catch (e: Exception) {
                Log.e("TerminalViewModel", "Failed to destroy pty", e)
                pty.destroyForcibly()
            }
        }
    }

    fun reboot(reason: String = "") {
        PlatformManager.moduleManager.reboot(reason)
    }

    fun onEmulatorCreated(emu: TerminalEmulator) {
        emulatorDeferred.complete(emu)
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
    protected fun devLog(
        data: ByteArray,
    ) {
        if (devMode) log(data)
    }

    @MainThread
    protected fun log(
        @StringRes message: Int,
        vararg format: Any? = emptyArray(),
    ) {
        if (format.isEmpty()) {
            log(
                message = context.getAnsiString(message).formatIfError(),
                log = localizedEnglishResources.getString(message),
            )

            return
        }

        log(
            message = context.getAnsiString(message, *format).formatIfError(),
            log = localizedEnglishResources.getString(message, *format),
        )
    }

    @MainThread
    protected fun log(
        message: String,
        log: String = message,
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            val emu = emulatorDeferred.await()
            emu.appendLineOnMain(message)
            logs += log

        }
    }

    @MainThread
    protected fun log(
        data: ByteArray,
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            val emu = emulatorDeferred.await()
            emu.appendTextOnMain(data.formatIfError())
            logs += data.contentToString()
        }
    }

    private val errorAnsiColor = "\u001B[0;38;5;168;49m"
    private val ansiReset = "\u001B[0m"

    fun String.formatIfError(): String {
        if (isEmpty() || this[0] != '!') return this

        return errorAnsiColor + this + ansiReset
    }

    fun ByteArray.formatIfError(): ByteArray {
        if (isEmpty() || this[0] != '!'.code.toByte()) return this

        return errorAnsiColor.encodeToByteArray() + this + ansiReset.encodeToByteArray()
    }

    private fun Context.getAnsiString(
        @StringRes resId: Int,
        vararg formatArgs: Any?,
    ): String {
        return getString(resId, *formatArgs)
            .replace("\\x1b", "\u001B")
    }

    suspend fun CompletableDeferred<Pty>.isSuccess() = await().waitForAsync() == 0
}
