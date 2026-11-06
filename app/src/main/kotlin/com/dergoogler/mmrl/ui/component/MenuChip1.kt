package com.dergoogler.mmrl.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.mmrlx.compose.ui.chip.FilterChip
import dev.mmrlx.compose.ui.chip.FilterChipDefaults
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.theme.LocalContentColor

@Composable
fun MenuChip1(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier.height(FilterChipDefaults.Height),
        enabled = enabled,
        leadingIcon = {
            if (!selected) {
                Point1(size = 8.dp)
            }
        },
        trailingIcon = {
            if (selected) {
                Icon(
                    painter = painterResource(dev.mmrlx.ui.R.drawable.done),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        },
        shape = CircleShape,
    )
}

@Composable
private fun Point1(
    size: Dp,
    color: Color = LocalContentColor.current,
) = Canvas(
    modifier = Modifier.size(size),
) {
    drawCircle(
        color = color,
        radius = this.size.width / 2,
        center = this.center,
    )
}
