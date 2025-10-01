package com.dergoogler.mmrl.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import dev.chrisbanes.haze.rememberHazeState

object LocalScreen {
    @Composable
    fun SnackbarHost(
        modifier: Modifier = Modifier,
        snackbar: @Composable (SnackbarData) -> Unit = { Snackbar(it) },
    ) {
        val host = LocalSnackbarHost.current
        val paddingValues = LocalMainScreenInnerPaddings.current

        androidx.compose.material3.SnackbarHost(
            modifier = Modifier
                .padding(bottom = paddingValues.calculateBottomPadding())
                .then(modifier),
            snackbar = snackbar,
            hostState = host
        )
    }
}

/**
 * Provides the screen with composition locals that shouldn't be used globally.
 */
@Composable
fun LocalScreenProvider(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSnackbarHost provides remember { SnackbarHostState() },
        LocalHazeState provides rememberHazeState()
    ) {
        content()
    }
}