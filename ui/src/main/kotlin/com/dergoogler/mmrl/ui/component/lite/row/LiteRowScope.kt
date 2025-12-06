package com.dergoogler.mmrl.ui.component.lite.row

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.unit.Density

@Immutable
enum class VerticalAlignment {
    Top,
    Center,
    Bottom,
}

@Immutable
enum class HorizontalAlignment {
    Start,
    Center,
    End,
}

typealias LiteHorizontalRowAlignment = HorizontalAlignment

@LayoutScopeMarker
@Immutable
interface LiteRowScope {
    @Stable
    fun Modifier.align(
        verticalAlignment: VerticalAlignment = VerticalAlignment.Top,
        horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Start,
    ): Modifier

    @Stable
    fun Modifier.weight(
        weight: Float,
        fill: Boolean = true,
    ): Modifier
}

object LiteRowScopeInstance : LiteRowScope {
    @Stable
    override fun Modifier.align(
        verticalAlignment: VerticalAlignment,
        horizontalAlignment: HorizontalAlignment,
    ): Modifier = this.then(LiteRowChildDataElement(verticalAlignment, horizontalAlignment))

    @Stable
    override fun Modifier.weight(
        weight: Float,
        fill: Boolean,
    ): Modifier = this.then(LiteRowChildWeightDataElement(weight, fill))
}

@Stable
class LiteRowChildData(
    var verticalAlignment: VerticalAlignment = VerticalAlignment.Top,
    var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Start,
    var weight: Float = 0f,
    var fill: Boolean = true,
)

@Immutable
class LiteRowChildDataNodeAlignment(
    var verticalAlignment: VerticalAlignment,
    var horizontalAlignment: HorizontalAlignment,
) : Modifier.Node(),
    ParentDataModifierNode {
    override fun Density.modifyParentData(parentData: Any?): LiteRowChildData =
        ((parentData as? LiteRowChildData) ?: LiteRowChildData()).also {
            it.verticalAlignment = verticalAlignment
            it.horizontalAlignment = horizontalAlignment
        }
}

@Immutable
private class LiteRowChildDataElement(
    val verticalAlignment: VerticalAlignment,
    val horizontalAlignment: HorizontalAlignment,
) : ModifierNodeElement<LiteRowChildDataNodeAlignment>() {
    override fun create(): LiteRowChildDataNodeAlignment =
        LiteRowChildDataNodeAlignment(
            verticalAlignment = verticalAlignment,
            horizontalAlignment = horizontalAlignment,
        )

    override fun update(node: LiteRowChildDataNodeAlignment) {
        node.verticalAlignment = verticalAlignment
        node.horizontalAlignment = horizontalAlignment
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? LiteRowChildDataElement ?: return false
        return verticalAlignment == otherModifier.verticalAlignment &&
            horizontalAlignment == otherModifier.horizontalAlignment
    }

    override fun hashCode(): Int = (verticalAlignment.hashCode() + horizontalAlignment.hashCode() * 31)
}

@Immutable
class LiteRowChildDataNodeWeight(
    var weight: Float = 0f,
    var fill: Boolean = true,
) : Modifier.Node(),
    ParentDataModifierNode {
    override fun Density.modifyParentData(parentData: Any?): LiteRowChildData =
        ((parentData as? LiteRowChildData) ?: LiteRowChildData()).also {
            it.weight = weight
            it.fill = fill
        }
}

@Immutable
private class LiteRowChildWeightDataElement(
    val weight: Float,
    val fill: Boolean,
) : ModifierNodeElement<LiteRowChildDataNodeWeight>() {
    override fun create(): LiteRowChildDataNodeWeight =
        LiteRowChildDataNodeWeight(
            weight = weight,
            fill = fill,
        )

    override fun update(node: LiteRowChildDataNodeWeight) {
        node.weight = weight
        node.fill = fill
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? LiteRowChildWeightDataElement ?: return false
        return weight == otherModifier.weight && fill == otherModifier.fill
    }

    override fun hashCode(): Int = (weight.hashCode() * 31 + fill.hashCode())
}

@Stable
val Measurable.liteRowChildDataNode: LiteRowChildData? get() = parentData as? LiteRowChildData
