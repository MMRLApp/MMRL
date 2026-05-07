package com.dergoogler.mmrl.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.component.toolbar.ToolbarEventIcon
import com.dergoogler.mmrl.ui.model.toolbar.ToolbarEvent

@Composable
fun TopAppBarEventIcon() {
    val userPrefs = LocalUserPreferences.current

    val events =
        remember {
            mutableListOf(
                // R.I.P. Hamster
                ToolbarEvent(
                    start = "01-05 22:00",
                    end = "10-05 15:00",
                    icon = R.drawable.mickey,
                ),
                // MMRL Birthday
                ToolbarEvent(
                    start = "25-04 00:00",
                    end = "26-04 00:00",
                    icon = R.drawable.gift,
                ),
                // New years eve
                ToolbarEvent(
                    start = "31-12 14:00",
                    end = "01-01 14:00",
                    icon = R.drawable.sparkles,
                ),
                // Christmas
                ToolbarEvent(
                    start = "01-12 00:00",
                    end = "27-12 00:00",
                    icon = R.drawable.christmas_tree,
                ),
                // Halloween
                ToolbarEvent(
                    start = "31-10 00:00",
                    end = "01-11 00:00",
                    icon = R.drawable.pumpkin_scary,
                ),
            )
        }

    ToolbarEventIcon(
        enabled = userPrefs.enableToolbarEvents,
        events = events,
    )
}
