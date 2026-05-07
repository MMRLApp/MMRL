package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layoutId
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlotScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlotScopeInstance

@Composable
fun ListItemScope.Slot(
    slot: Any,
    modifier: Modifier = Modifier,
    disallow: List<Any> = emptyList(),
    content: @Composable ListItemSlotScope.() -> Unit,
) = Box(
    modifier =
        Modifier
            .layoutSlot(slot, disallow)
            .then(modifier),
) {
    val instance =
        remember {
            ListItemSlotScopeInstance(
                boxScope = this,
                contentPaddingValues = this@Slot.contentPaddingValues,
                iconSize = this@Slot.iconSize,
            )
        }

    instance.content()
}

@Composable
fun ListItemScope.FromSlot(
    slot: Any,
    content: @Composable ListItemScope.() -> Unit,
) = SubcomposeLayout { constraints ->
    val measurables =
        subcompose(slot) {
            content()
        }

    val measurable = measurables.firstOrNull { it.layoutId == slot }

    val placeable = measurable?.measure(constraints)

    layout(placeable?.width ?: 0, placeable?.height ?: 0) {
        placeable?.placeRelative(0, 0)
    }
}
