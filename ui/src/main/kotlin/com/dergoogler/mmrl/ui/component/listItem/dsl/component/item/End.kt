package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlotScope

/**
 * Composable function that defines the end slot of a list item.
 * This slot is typically used for trailing icons, actions, or other content
 * that should be aligned to the end of the list item.
 *
 * This function is a convenience wrapper around the `Slot` composable,
 * specifically configured for the `ListItemSlot.End` slot.
 *
 * @param modifier Optional [Modifier] to be applied to the end slot container.
 * @param content The composable content to be displayed in the end slot.
 *                This lambda is invoked in the context of a `ListItemSlotScope`,
 *                allowing for slot-specific configurations if needed.
 */
@Composable
fun ListItemScope.End(
    modifier: Modifier = Modifier,
    content: @Composable ListItemSlotScope.() -> Unit
) =
    Slot(
        modifier = modifier,
        slot = ListItemSlot.End,
        content = content
    )
