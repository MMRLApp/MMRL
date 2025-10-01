package com.dergoogler.mmrl.ui.screens.main

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.model.ModId.Companion.toModId
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.providable.LocalBulkInstall
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.remember.rememberLocalModule
import com.dergoogler.mmrl.ui.remember.rememberLocalModules
import com.dergoogler.mmrl.utils.initPlatform
import com.dergoogler.mmrl.viewmodel.BulkInstallViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs

@Composable
fun RootScreen() {
    val navController = LocalNavController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val bulkInstallViewModel: BulkInstallViewModel = hiltViewModel()
    val context = LocalContext.current
    val userPreferences = LocalUserPreferences.current

    LaunchedEffect(Unit) {
        initPlatform(context, userPreferences.workingMode.toPlatform())
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.none,
    ) { paddingValues ->
        CompositionLocalProvider(
            LocalSnackbarHost provides snackbarHostState,
            LocalBulkInstall provides bulkInstallViewModel
        ) {
            DestinationsNavHost(
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
                navGraph = NavGraphs.root,
                navController = navController,
                defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
                        get() = { fadeIn(animationSpec = tween(340)) }
                    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
                        get() = { fadeOut(animationSpec = tween(340)) }
                }
            )
        }
    }
}
