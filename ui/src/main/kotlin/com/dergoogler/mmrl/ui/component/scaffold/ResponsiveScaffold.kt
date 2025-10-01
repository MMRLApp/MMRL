package com.dergoogler.mmrl.ui.component.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dergoogler.mmrl.ext.currentScreenWidth

@Composable
fun ResponsiveScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit) = {},
    bottomBar: @Composable (() -> Unit) = {},
    railBar: @Composable (() -> Unit) = {},
    snackbarHost: @Composable (() -> Unit) = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (ScaffoldScope.(PaddingValues) -> Unit),
) {
    val width = currentScreenWidth()

    Scaffold(
        modifier = modifier,
        floatingActionButtonPosition = floatingActionButtonPosition,
        topBar = topBar,
        bottomBar = {
            if (width.isSmall) bottomBar()
        },
        snackbarHost = snackbarHost,
        contentWindowInsets = contentWindowInsets,
        floatingActionButton = floatingActionButton,
        railBar = {
            if (width.isMedium) railBar()
        },
        containerColor = containerColor,
        contentColor = contentColor,
        content = content
    )
}
