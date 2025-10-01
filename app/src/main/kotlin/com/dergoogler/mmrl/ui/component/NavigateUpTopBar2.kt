package com.dergoogler.mmrl.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigateUpTopBar(
    title: String,
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enable: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    bottomBorder: Boolean = true,
) = NavigateUpTopBar(
    modifier = modifier,
    title = title,
    subtitle = subtitle,
    onBack = { navigator.popBackStack() },
    actions = actions,
    windowInsets = windowInsets,
    colors = colors,
    scrollBehavior = scrollBehavior,
    enable = enable,
    bottomBorder = bottomBorder
)