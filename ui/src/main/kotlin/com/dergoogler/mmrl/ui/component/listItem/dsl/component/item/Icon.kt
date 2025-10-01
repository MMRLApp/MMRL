package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.DefaultIconSize

/**
 * A composable function that adds an icon to a list item.
 *
 * @param painter The painter to use for the icon.
 * @param size The size of the icon. Defaults to [DefaultIconSize].
 * @param slot The slot to place the icon in. Can be either [ListItemSlot.Start] or [ListItemSlot.End].
 * Defaults to [ListItemSlot.Start].
 * @throws IllegalArgumentException if the slot is not [ListItemSlot.Start] or [ListItemSlot.End].
 */
@Composable
fun ListItemScope.Icon(
    modifier: Modifier = Modifier,
    painter: Painter,
    size: Dp = iconSize,
    slot: ListItemSlot = ListItemSlot.Start,
) = Slot(
    slot = slot,
    disallow = listOf(ListItemSlot.Title, ListItemSlot.Description),
    content = {
        Icon(
            modifier = Modifier.size(size).then(modifier),
            painter = painter,
            contentDescription = null,
            tint = LocalContentColor.current
        )
    }
)

