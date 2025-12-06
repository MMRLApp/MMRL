package com.dergoogler.mmrl.ui.screens.settings.appearance.items

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.DarkMode

private enum class DarkModeItem(
    val value: DarkMode,
    val icon: Int,
    val text: Int,
) {
    Auto(
        value = DarkMode.FollowSystem,
        icon = R.drawable.brightness_2,
        text = R.string.app_theme_dark_theme_auto,
    ),

    Light(
        value = DarkMode.AlwaysOff,
        icon = R.drawable.sun,
        text = R.string.app_theme_dark_theme_light,
    ),

    Dark(
        value = DarkMode.AlwaysOn,
        icon = R.drawable.moon_stars,
        text = R.string.app_theme_dark_theme_dark,
    ),
}

private val modes =
    listOf(
        DarkModeItem.Auto,
        DarkModeItem.Light,
        DarkModeItem.Dark,
    )

@Composable
fun DarkModeItem(
    darkMode: DarkMode,
    onChange: (DarkMode) -> Unit,
) = LazyRow(
    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(15.dp),
) {
    items(
        items = modes,
        key = { it.value },
    ) {
        DarkModeItem(
            item = it,
            darkMode = darkMode,
        ) { value ->
            onChange(value)
        }
    }
}

@Composable
private fun DarkModeItem(
    item: DarkModeItem,
    darkMode: DarkMode,
    onClick: (DarkMode) -> Unit,
) {
    val selected = item.value == darkMode

    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(15.dp))
                .clickable(
                    onClick = { onClick(item.value) },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ).background(color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val animateZ by animateFloatAsState(
                targetValue = if (selected) 0f else 360f,
                animationSpec =
                    tween(
                        durationMillis = 350,
                        easing = FastOutSlowInEasing,
                    ),
                label = "animateZ",
            )

            Icon(
                modifier =
                    Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            rotationZ = if (selected) animateZ else 0f
                        },
                painter = painterResource(id = item.icon),
                contentDescription = null,
                tint =
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        LocalContentColor.current
                    },
            )

            Text(
                text = stringResource(id = item.text),
                style = MaterialTheme.typography.labelLarge,
                color =
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Unspecified
                    },
            )
        }
    }
}
