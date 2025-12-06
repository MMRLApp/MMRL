package com.dergoogler.mmrl.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MenuChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = FilterChip(
    selected = selected,
    onClick = onClick,
    label = label,
    modifier = modifier.height(FilterChipDefaults.Height),
    enabled = enabled,
    leadingIcon = {
        if (!selected) {
            Point(size = 8.dp)
        }
    },
    trailingIcon = {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize),
            )
        }
    },
    shape = CircleShape,
    colors =
        FilterChipDefaults.filterChipColors(
            iconColor = MaterialTheme.colorScheme.secondary,
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
            selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondary,
        ),
    border =
        FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.secondary,
        ),
)

@Composable
private fun Point(
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
