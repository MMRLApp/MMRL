package com.dergoogler.mmrl.ui.component.listItem.dsl

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Dp

enum class ListItemSlot {
    Start,
    End,
    Title,
    Description,
    Supporting,
}

enum class DialogItemSlot {
    Description,
    SupportingText,
    TextField,
}

@LayoutScopeMarker
@Immutable
interface BaseListScope {
    val contentPaddingValues: PaddingValues
    val iconSize: Dp
}

@LayoutScopeMarker
@Immutable
interface ListItemScope : BaseListScope {
    fun Modifier.layoutSlot(
        slot: Any,
        disallow: List<Any> = emptyList(),
    ): Modifier
}

@LayoutScopeMarker
@Immutable
interface ListItemSlotScope : BaseListScope

@LayoutScopeMarker
@Immutable
interface ListScope : BaseListScope

internal class ListScopeInstance(
    private val columnScope: ColumnScope,
    override val contentPaddingValues: PaddingValues,
    override val iconSize: Dp,
) : ListScope,
    ColumnScope by columnScope

internal class ListItemScopeInstance(
    override val contentPaddingValues: PaddingValues,
    override val iconSize: Dp,
) : ListItemScope {
    override fun Modifier.layoutSlot(
        slot: Any,
        disallow: List<Any>,
    ): Modifier {
        if (disallow.contains(slot)) {
            throw IllegalStateException("Slot $slot is not allowed in this scope")
        }

        return this.layoutId(slot)
    }
}

internal class ListItemSlotScopeInstance(
    private val boxScope: BoxScope,
    override val contentPaddingValues: PaddingValues,
    override val iconSize: Dp,
) : ListItemSlotScope,
    BoxScope by boxScope

val LocalListItemEnabled = staticCompositionLocalOf { true }
