package com.dergoogler.mmrl.ui.component

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ui.navigation.MainDestination
import com.dergoogler.mmrl.ui.navigation.isAccessible
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.navigationbar.NavigationBar
import dev.mmrlx.compose.ui.navigationbar.NavigationBarItem
import dev.mmrlx.compose.ui.theme.MMRLXTheme

@Composable
fun BottomBar(updates: Int = 0) {
    val prefs = LocalUserPreferences.current
    val navigator = LocalDestinationsNavigator.current
    val navController = LocalNavController.current

    NavigationBar(
        modifier =
            Modifier
                .imePadding()
    ) {
        for (screen in MainDestination.entries) {
            if (!screen.isAccessible) continue

            val isSelected by navController.isRouteOnBackStackAsState(screen.direction)

            NavigationBarItem(
                icon = {
                    BaseNavIcon(screen, isSelected, updates)
                },
                label = {
                    if (!prefs.hideBottomBarLabels) {
                        Text(
                            text = stringResource(id = screen.label),
                            style = MMRLXTheme.typography.labelLarge,
                        )
                    }
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
/*
@Composable
private fun RailNav(updates: Int) {
    val prefs = LocalUserPreferences.current
    val navigator = LocalDestinationsNavigator.current
    val navController = LocalNavController.current

    NavigationRail(
        header = {
            TopAppBarEventIcon()
        },
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
                        style = MaterialTheme.typography.labelLarge,
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
                },
            )
        }
    }
}*/

@Composable
private fun BaseNavIcon(
    screen: MainDestination,
    selected: Boolean,
    updates: Int,
) {
    if (screen == MainDestination.Modules && updates > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(text = updates.toString())
                }
            },
        ) {
            Icon(
                painter =
                    painterResource(
                        id =
                            if (selected) {
                                screen.iconFilled
                            } else {
                                screen.icon
                            },
                    ),
                contentDescription = null,
            )
        }

        return
    }

    Icon(
        painter =
            painterResource(
                id =
                    if (selected) {
                        screen.iconFilled
                    } else {
                        screen.icon
                    },
            ),
        contentDescription = null,
    )
}