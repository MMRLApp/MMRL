package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const.CLEAR_CMD
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.terminal.TextBlock
import com.dergoogler.mmrl.model.terminal.commands.AddMask
import com.dergoogler.mmrl.model.terminal.commands.Card
import com.dergoogler.mmrl.model.terminal.commands.EndCard
import com.dergoogler.mmrl.model.terminal.commands.EndGroup
import com.dergoogler.mmrl.model.terminal.commands.Error
import com.dergoogler.mmrl.model.terminal.commands.Group
import com.dergoogler.mmrl.model.terminal.commands.Notice
import com.dergoogler.mmrl.model.terminal.commands.RemoveLine
import com.dergoogler.mmrl.model.terminal.commands.ReplaceSelf
import com.dergoogler.mmrl.model.terminal.commands.Warning
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.ui.activity.terminal.ActionCommand
import com.dergoogler.mmrl.ui.activity.terminal.Terminal
import com.dergoogler.mmrl.utils.initPlatform
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

        val terminal by mutableStateOf(Terminal())

        private val localFlow = MutableStateFlow<LocalModule?>(null)
        val local get() = localFlow.asStateFlow()

        protected val platformReadyDeferred = CompletableDeferred<Boolean>()

        private val commands =
            listOf(
                Group(),
                EndGroup(),
                Card(),
                EndCard(),
                AddMask(),
                Notice(),
                Warning(),
                Error(),
                ReplaceSelf(), // SetLines()
                RemoveLine(),
            )

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
                    terminal.event = Event.FAILED
                    log(R.string.failed_to_initialize_platform)
                    platformReadyDeferred.complete(false)
                } else {
                    devLog(R.string.platform_initialized)
                    platformReadyDeferred.complete(true)
                }
            }
        }

        override fun onCleared() {
            terminal.shell.close()
            terminal.currentCard = null
            terminal.currentGroup = null
            super.onCleared()
        }

        fun reboot(reason: String = "") {
            PlatformManager.moduleManager.reboot(reason)
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
            with(terminal) {
                viewModelScope.launch(Dispatchers.Main) {
                    if (message.startsWith(CLEAR_CMD)) {
                        console.clear()
                        logs.clear()
                        lineNumber = 1
                        currentGroup = null
                        currentCard = null
                        lineAdded = true
                        return@launch
                    }

                    val maskedMessage = message.fixNewLines.applyMasks
                    val maskedLog = log.fixNewLines.applyMasks

                    val command =
                        ActionCommand.tryParseV2AndRun(
                            message = message,
                            terminal = this@with,
                            registeredCommands = commands,
                        )

                    if (!command) {
                        emitLogLine(maskedMessage)
                    }

                    logs += maskedLog

                    if (lineAdded) {
                        lineNumber++
                    }

                    // Reset the flag for next line
                    lineAdded = true
                }
            }
        }

        private fun emitLogLine(text: String) {
            with(terminal) {
                val entry = lineNumber to text
                when {
                    currentGroup != null -> {
                        currentGroup!!.lines += entry
                    }

                    currentCard != null -> {
                        currentCard!!.lines += entry
                    }

                    else -> {
                        console += TextBlock(lineNumber, text)
                    }
                }
            }
        }
    }
