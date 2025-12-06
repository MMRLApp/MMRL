package com.dergoogler.mmrl.ui.component.scaffold

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
class FabPlacement(
    val left: Int,
    val width: Int,
    val height: Int,
)

val FabSpacing = 16.dp

enum class ScaffoldLayoutContent {
    TopBar,
    Snackbar,
    Fab,
    BottomBar,
    NavigationRail,
    MainContent,
}
