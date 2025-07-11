package com.dergoogler.mmrl.ui.activity.terminal.install

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.app.Event.Companion.isFinished
import com.dergoogler.mmrl.app.Event.Companion.isLoading
import com.dergoogler.mmrl.app.Event.Companion.isSucceeded
import com.dergoogler.mmrl.ext.isScrollingUp
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ui.component.Console
import com.dergoogler.mmrl.ui.component.NavigateUpTopBar
import com.dergoogler.mmrl.ui.component.dialog.ConfirmDialog
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.InstallViewModel
import com.dergoogler.mmrl.ui.activity.MMRLComponentActivity
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.terminal.TerminalView
import kotlinx.coroutines.launch

@Composable
fun InstallScreen(
    viewModel: InstallViewModel,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val userPreferences = LocalUserPreferences.current
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val isScrollingUp by listState.isScrollingUp()
    val showFab by remember {
        derivedStateOf {
            isScrollingUp && viewModel.terminal.event.isSucceeded
        }
    }

    var confirmReboot by remember { mutableStateOf(false) }
    var cancelInstall by remember { mutableStateOf(false) }

    val shell = viewModel.terminal.shell
    val event = viewModel.terminal.event

    val allowCancel = userPreferences.allowCancelInstall

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    val backHandler = {
        if (allowCancel) {
            when {
                event.isLoading && shell.isAlive -> cancelInstall = true
                event.isFinished -> (context as MMRLComponentActivity).finish()
            }
        } else {
            if (event.isFinished) {
                (context as MMRLComponentActivity).finish()
            }
        }
    }

    BackHandler(
        enabled = if (!allowCancel) event.isLoading else true,
        onBack = backHandler
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            viewModel.writeLogsTo(uri)
                .onSuccess {
                    val message = context.getString(R.string.install_logs_saved)
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }.onFailure {
                    val message = context.getString(
                        R.string.install_logs_save_failed,
                        it.message ?: context.getString(R.string.unknown_error)
                    )
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
        }
    }

    if (confirmReboot) ConfirmDialog(
        title = R.string.install_screen_reboot_title,
        description = R.string.install_screen_reboot_text,
        onClose = { confirmReboot = false },
        onConfirm = {
            confirmReboot = false
            viewModel.reboot()
        }
    )

    if (cancelInstall) ConfirmDialog(
        title = R.string.install_screen_cancel_title,
        description = R.string.install_screen_cancel_text,
        onClose = { cancelInstall = false },
        onConfirm = {
            scope.launch {
                cancelInstall = false
                shell.close()
            }
        }
    )

    Scaffold(
        modifier = Modifier
            .onKeyEvent {
                when (it.key) {
                    Key.VolumeUp, Key.VolumeDown -> event.isLoading

                    else -> false
                }
            }
            .focusRequester(focusRequester)
            .focusable()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                exportLog = { launcher.launch(viewModel.logfile) },
                event = event,
                enable = if (!allowCancel) event.isFinished else true,
                scrollBehavior = scrollBehavior,
                onBack = backHandler
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn(
                    animationSpec = tween(100),
                    initialScale = 0.8f
                ),
                exit = scaleOut(
                    animationSpec = tween(100),
                    targetScale = 0.8f
                )
            ) {
                FloatingButton(
                    reboot = {
                        if (userPreferences.confirmReboot) {
                            confirmReboot = true
                        } else {
                            viewModel.reboot()
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.none
    ) {
        TerminalView(
            terminal = viewModel.terminal,
            state = listState,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        )
    }
}

@Composable
private fun TopBar(
    exportLog: () -> Unit,
    event: Event,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    enable: Boolean,
) = NavigateUpTopBar(
    title = stringResource(id = R.string.install_screen_title),
    subtitle = stringResource(
        id = when (event) {
            Event.LOADING -> R.string.install_flashing
            Event.FAILED -> R.string.install_failure
            else -> R.string.install_done
        }
    ),
    enable = enable,
    onBack = onBack,
    scrollBehavior = scrollBehavior,
    actions = {
        if (event.isFinished) {
            IconButton(
                onClick = exportLog
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.device_floppy),
                    contentDescription = null
                )
            }
        }

    }
)

@Composable
private fun FloatingButton(
    reboot: () -> Unit,
) = FloatingActionButton(
    onClick = reboot
) {
    Icon(
        painter = painterResource(id = R.drawable.reload),
        contentDescription = null
    )
}
