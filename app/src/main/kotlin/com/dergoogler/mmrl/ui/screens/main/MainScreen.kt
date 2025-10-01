package com.dergoogler.mmrl.ui.screens.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.dergoogler.mmrl.ext.currentScreenWidth
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ui.component.TopAppBar
import com.dergoogler.mmrl.ui.component.TopAppBarEventIcon
import com.dergoogler.mmrl.ui.component.scaffold.ResponsiveScaffold
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurBottomToolbar
import com.dergoogler.mmrl.ui.navigation.MainDestination
import com.dergoogler.mmrl.ui.navigation.isAccessible
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
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Destination<RootGraph>
@Composable
fun MainScreen() {
    val width = currentScreenWidth()
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
        LocalBulkInstall provides bulkInstallViewModel
    ) {
        if (width.isLarge) {
            Scaffold(
                contentWindowInsets = WindowInsets.none
            ) { paddingValues ->
                val navController = LocalNavController.current

                PermanentNavigationDrawer(
                    drawerContent = {
                        PermanentDrawerSheet(
                            modifier = Modifier
                                .width(240.dp)
                        ) {
                            TopAppBar(
                                title = {
                                    TopAppBarEventIcon()
                                },
                            )

                            LazyColumn(
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(
                                    items = MainDestination.entries,
                                    key = { it.direction }
                                ) { screen ->
                                    val isSelected by navController.isRouteOnBackStackAsState(screen.direction)

                                    if (!screen.isAccessible) return@items

                                    NavigationDrawerItem(
                                        icon = {
                                            BaseNavIcon(screen, isSelected, updates)
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(id = screen.label),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) {
                                                navigator.popBackStack(screen.direction, false)
                                            }
                                            navigator.navigate(screen.direction) {
                                                popUpTo(NavGraphs.root) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                    )
                                }
                            }

                        }
                    }
                ) {
                    CurrentNavHost(
                        Modifier.padding(paddingValues)
                    )
                }
            }

            return@CompositionLocalProvider
        }


        ResponsiveScaffold(
            bottomBar = {
                BottomNav(updates)
            },
            railBar = {
                RailNav(updates)
            },
            contentWindowInsets = WindowInsets.none
        ) { paddingValues ->
            CompositionLocalProvider(
                LocalMainScreenInnerPaddings provides paddingValues,
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
        defaultTransitions = object : NavHostAnimatedDestinationStyle() {
            override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
                get() = { fadeIn(animationSpec = tween(340)) }
            override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
                get() = { fadeOut(animationSpec = tween(340)) }
        }
    )
}

@Composable
private fun BottomNav(updates: Int) {
    val prefs = LocalUserPreferences.current
    val navigator = LocalDestinationsNavigator.current
    val navController = LocalNavController.current

    BlurBottomToolbar(
        modifier = Modifier
            .imePadding()
    ) {
        MainDestination.entries.forEach { screen ->
            val isSelected by navController.isRouteOnBackStackAsState(screen.direction)

            if (!screen.isAccessible) return@forEach

            NavigationBarItem(
                icon = {
                    BaseNavIcon(screen, isSelected, updates)
                },
                label = {
                    Text(
                        text = stringResource(id = screen.label),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                alwaysShowLabel = !prefs.hideBottomBarLabels,
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        navigator.popBackStack(screen.direction, false)
                    }
                    navigator.navigate(screen.direction) {
                        popUpTo(NavGraphs.root) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun RailNav(updates: Int) {
    val prefs= LocalUserPreferences.current
    val navigator = LocalDestinationsNavigator.current
    val navController = LocalNavController.current

    NavigationRail(
        header = {
            TopAppBarEventIcon()
        }
    ) {
        MainDestination.entries.forEach { screen ->
            val isSelected by navController.isRouteOnBackStackAsState(screen.direction)

            if (!screen.isAccessible) return@forEach

            NavigationRailItem(
                icon = {
                    BaseNavIcon(screen, isSelected, updates)
                },
                label = {
                    Text(
                        text = stringResource(id = screen.label),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                alwaysShowLabel = !prefs.hideBottomBarLabels,
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        navigator.popBackStack(screen.direction, false)
                    }
                    navigator.navigate(screen.direction) {
                        popUpTo(NavGraphs.root) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun BaseNavIcon(screen: MainDestination, selected: Boolean, updates: Int) {
    if (screen == MainDestination.Modules && updates > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(text = updates.toString())
                }
            }
        ) {
            Icon(
                painter = painterResource(
                    id = if (selected) {
                        screen.iconFilled
                    } else {
                        screen.icon
                    }
                ),
                contentDescription = null,
            )
        }

        return
    }

    Icon(
        painter = painterResource(
            id = if (selected) {
                screen.iconFilled
            } else {
                screen.icon
            }
        ),
        contentDescription = null,
    )
}