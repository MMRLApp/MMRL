package com.dergoogler.mmrl.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.model.toolbar.ToolbarEvent
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.theme.LocalContentColor
import dev.mmrlx.compose.ui.toolbar.ToolbarTitle

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

@Composable
fun ToolbarIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = com.dergoogler.mmrl.ui.R.drawable.mmrl_logo,
    subtitle: String? = null,
    tint: Color = MaterialTheme.colorScheme.surfaceTint,
) = ToolbarTitle(
    modifier = modifier,
    titleContent = {
        Icon(
            modifier = Modifier.size(30.dp),
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = tint,
        )
    },
    subtitleContent = {
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = LocalContentColor.current.copy(alpha = 0.75f),
            )
        }
    }
)

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

