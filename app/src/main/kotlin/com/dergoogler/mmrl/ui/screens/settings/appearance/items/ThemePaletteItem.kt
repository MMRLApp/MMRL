package com.dergoogler.mmrl.ui.screens.settings.appearance.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.theme.Colors
import dev.dergoogler.mmrl.compat.BuildCompat

@Composable
fun ThemePaletteItem(
    themeColor: Int,
    isDarkMode: Boolean,
    onChange: (Int) -> Unit,
) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (BuildCompat.atLeastS) {
            ThemeColorItem(
                id = Colors.Dynamic.id,
                themeColor = themeColor,
                isDarkMode = isDarkMode,
            ) {
                onChange(it)
            }
        }

        Colors.getColorIds().forEach {
            ThemeColorItem(
                id = it,
                themeColor = themeColor,
                isDarkMode = isDarkMode,
            ) { value ->
                onChange(value)
            }
        }
    }
}

@Composable
private fun ThemeColorItem(
    id: Int,
    themeColor: Int,
    isDarkMode: Boolean,
    onClick: (Int) -> Unit,
) {
    val color = Colors.getColor(id)
    val colorScheme = if (isDarkMode) color.darkColorScheme else color.lightColorScheme
    val selected = id == themeColor

    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(15.dp))
                .clickable(
                    onClick = { onClick(id) },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ).background(
                    color = colorScheme.surfaceColorAtElevation(3.dp),
                ).size(60.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .clip(CircleShape)
                    .fillMaxSize(0.75f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f)
                            .background(color = colorScheme.primaryContainer),
                )

                Spacer(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(color = colorScheme.tertiaryContainer),
                )
            }

            Spacer(
                modifier =
                    Modifier
                        .fillMaxSize(0.5f)
                        .clip(CircleShape)
                        .background(
                            color =
                                if (selected) {
                                    colorScheme.onPrimary
                                } else {
                                    colorScheme.primary
                                },
                        ),
            )

            if (selected) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    painter = painterResource(id = R.drawable.circle_check_filled),
                    contentDescription = null,
                    tint = colorScheme.primary,
                )
            }
        }
    }
}
