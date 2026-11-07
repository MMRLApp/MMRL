package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasAction
import com.dergoogler.mmrl.platform.content.State
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mmrlx.compose.ui.theme.Colors
import dev.mmrlx.compose.ui.theme.MMRLXTheme
import dev.mmrlx.terminal.PtyListener
import dev.mmrlx.terminal.launchSuperUserPty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@HiltViewModel(assistedFactory = ActionViewModel.Factory::class)
class ActionViewModel
@AssistedInject
constructor(
    @Assisted mxColors: Colors,
    @Assisted mdColors: ColorScheme,
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : TerminalViewModel(
    mxColors,
    mdColors,
    application,
    localRepository,
    modulesRepository,
    userPreferencesRepository
) {
    val logfile get() = "Action_${LocalDateTime.now()}.log"

    private val listener = object : PtyListener {
        override fun onStdout(data: ByteArray) {
            log(data)
        }

        override fun onStderr(data: ByteArray) {
            devLog(data)
        }
    }

    suspend fun runAction(modId: ModId) =
        withContext(Dispatchers.IO) {
            val module = localModule(modId.toString())
            val userPreferences = userPreferencesRepository.data.first()
            event = Event.LOADING

            if (module == null) {
                withContext(Dispatchers.Main) {
                    event = Event.FAILED
                    log(R.string.module_not_found)
                }
                return@withContext
            }

            if (!module.hasAction) {
                withContext(Dispatchers.Main) {
                    event = Event.FAILED
                    log(R.string.this_module_don_t_have_an_action)
                }
                return@withContext
            }

            if (module.state == State.DISABLE || module.state == State.REMOVE) {
                withContext(Dispatchers.Main) {
                    event = Event.FAILED
                    log(R.string.module_is_disabled_or_removed_unable_to_execute_action)
                }
                return@withContext
            }

            val success = executeAction(modId, userPreferences.useShellForModuleAction)

            event = if (success) Event.SUCCEEDED else Event.FAILED
        }

    private suspend fun executeAction(
        modId: ModId,
        legacy: Boolean,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val env = mapOf(
                "ASH_STANDALONE" to "1",
                "MMRL" to "true",
                "MMRL_VER" to BuildConfig.VERSION_NAME,
                "MMRL_VER_CODE" to BuildConfig.VERSION_CODE.toString(),
                "BOOTMODE" to "true",
                "ARCH" to Build.SUPPORTED_ABIS.firstOrNull().orEmpty(),
                "API" to Build.VERSION.SDK_INT.toString(),
                "IS64BIT" to Build.SUPPORTED_64_BIT_ABIS.isNotEmpty().toString(),
            )

            val command =
                if (legacy || platform.isMagisk) {
                    "busybox sh /data/adb/modules/$modId/action.sh"
                } else {
                    PlatformManager.moduleManager.getActionCommand(modId)
                }

            val emu = emulatorDeferred.await()

            ptyDeferred.complete(
                emu.launchSuperUserPty(
                    scope = viewModelScope,
                    command = command,
                    env = env,
                    listener = listener
                )
            )

            return@withContext ptyDeferred.isSuccess()
        }

    @AssistedFactory
    interface Factory {
        fun create(
            mxColors: Colors,
            mdColors: ColorScheme,
        ): ActionViewModel
    }

    companion object {
        @Composable
        fun build(
            mxColors: Colors,
            mdColors: ColorScheme,
        ): ActionViewModel =
            androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<ActionViewModel, Factory> { factory ->
                factory.create(mxColors, mdColors)
            }

        private val local =
            staticCompositionLocalOf<ActionViewModel> { error("No ActionViewModel found!") }

        @get:Composable
        val current: ActionViewModel get() = local.current

        @Composable
        fun LocalProvider(content: @Composable (ActionViewModel) -> Unit) {
            val mxColors = MMRLXTheme.colors
            val mdColors = MaterialTheme.colorScheme

            val vm = build(mxColors, mdColors)
            CompositionLocalProvider(
                local provides vm,
            ) {
                content(vm)
            }
        }
    }
}
