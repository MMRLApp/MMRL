package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlotScope

/**
 * A composable function that defines the content to be placed at the start of a [ListItemScope].
 * This is typically used for elements like icons, avatars, or checkboxes.
 *
 * The content is scoped to [ListItemSlotScope], providing access to slot-specific modifiers and information.
 *
 * @param modifier Optional [Modifier] to be applied to the start slot container.
 * @param content The composable lambda that defines the UI elements to be displayed in the start slot.
 *                It is executed within a [ListItemSlotScope].
 */
@Composable
fun ListItemScope.Start(
    modifier: Modifier = Modifier,
    content: @Composable ListItemSlotScope.() -> Unit
) =
    Slot(
        modifier = modifier,
        slot = ListItemSlot.Start,
        content = content
    )
