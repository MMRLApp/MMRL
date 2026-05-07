package com.dergoogler.mmrl.ui.component.card

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId

enum class CardSlot {
    Relative,
    Absolute,
}

@Immutable
internal data class CardLayoutId(
    val slot: CardSlot,
    val alignment: Alignment? = null,
)

@LayoutScopeMarker
@Immutable
interface CardScope {
    /**
     * Set this content to be laid out within the Card and position it absolutely relative to
     * the card.
     *
     * In this example, the settings icon is positioned at the top-end corner of the card.
     *
     * @param alignment the alignment of this item in the card.
     */
    fun Modifier.absolute(alignment: Alignment = Alignment.TopStart): Modifier

    /**
     * Places the layout element in the relative slot of the [Card]
     * - The relative slot is placed after the main content of the card.
     * - The relative slot has the same width as the card, but its height is determined by its content.
     */
    fun Modifier.relative(): Modifier
}

internal class CardScopeInstance : CardScope {
    override fun Modifier.absolute(alignment: Alignment) = layoutId(CardLayoutId(slot = CardSlot.Absolute, alignment = alignment))

    override fun Modifier.relative() = layoutId(CardLayoutId(slot = CardSlot.Relative))
}

internal class CardAbsoluteScopeInstance(
    private val cardScope: CardScope,
    private val boxScope: BoxScope,
) : CardScope by cardScope,
    BoxScope by boxScope
