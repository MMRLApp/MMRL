package com.dergoogler.mmrl.ui.screens.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ui.providable.LocalBulkInstall
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.remember.rememberUpdatableModuleCount
import com.dergoogler.mmrl.utils.initPlatform
import com.dergoogler.mmrl.viewmodel.BulkInstallViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.mmrlx.compose.ui.scaffold.Scaffold

@Composable
fun MainScreen() {
//    val width = currentScreenWidth()
    val userPrefs = LocalUserPreferences.current
    val navigator = LocalDestinationsNavigator.current
    val context = LocalContext.current
    val updates by rememberUpdatableModuleCount()

    val snackbarHostState = remember { SnackbarHostState() }
    val bulkInstallViewModel: BulkInstallViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        initPlatform(context, userPrefs.workingMode.toPlatform())
    }

    val hazeState = rememberHazeState()

    CompositionLocalProvider(
        LocalHazeState provides hazeState,
        LocalSnackbarHost provides snackbarHostState,
        LocalBulkInstall provides bulkInstallViewModel,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.none,
        ) {
            CompositionLocalProvider(
                LocalMainScreenInnerPaddings provides contentPadding,
            ) {
                CurrentNavHost(
                    modifier = Modifier.hazeSource(hazeState),
                )
            }
        }
    }
}

@Composable
private fun CurrentNavHost(modifier: Modifier = Modifier) {
    val navController = LocalNavController.current
    DestinationsNavHost(
        modifier = modifier,
        navGraph = NavGraphs.root,
        navController = navController,
        defaultTransitions =
            object : NavHostAnimatedDestinationStyle() {
                override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
                    get() = { fadeIn(animationSpec = tween(340)) }
                override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
                    get() = { fadeOut(animationSpec = tween(340)) }
            },
    )
}

