package com.dergoogler.mmrl.ui.component.toolbar

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.ui.model.toolbar.ToolbarEvent

@Composable
fun ToolbarEventIcon(
    events: List<ToolbarEvent>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes nonEventIcon: Int = com.dergoogler.mmrl.ui.R.drawable.mmrl_logo,
) {
    if (!enabled) {
        return ToolbarIcon(
            modifier = modifier,
            icon = nonEventIcon,
        )
    }

    val event = remember(events) { events.firstOrNull { it.isActive } }

    if (event != null) {
        ToolbarIcon(
            modifier = modifier,
            icon = event.icon,
            subtitle = event.getTitle(),
        )
    } else {
        ToolbarIcon(
            modifier = modifier,
            icon = nonEventIcon,
        )
    }
}
