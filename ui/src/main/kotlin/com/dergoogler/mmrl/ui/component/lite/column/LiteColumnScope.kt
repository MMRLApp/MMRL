package com.dergoogler.mmrl.ui.component.lite.column

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.unit.Density

@Immutable
enum class HorizontalAlignment {
    Start, Center, End
}

typealias LiteColumnHorizontalAlignment = HorizontalAlignment

@LayoutScopeMarker
@Immutable
interface LiteColumnScope {
    @Stable
    fun Modifier.align(alignment: HorizontalAlignment): Modifier

    @Stable
    fun Modifier.weight(weight: Float): Modifier
}

object LiteColumnScopeInstance : LiteColumnScope {
    @Stable
    override fun Modifier.align(alignment: HorizontalAlignment): Modifier =
        this.then(LiteColumnChildDataElement(alignment))

    @Stable
    override fun Modifier.weight(weight: Float): Modifier =
        this.then(LiteColumnWeightElement(weight))
}

@Immutable
class LiteColumnChildData(
    var alignment: HorizontalAlignment = HorizontalAlignment.Start,
    var weight: Float = 0f
)

@Immutable
class LiteColumnChildDataAlignment(
    var alignment: HorizontalAlignment = HorizontalAlignment.Start,
) : ParentDataModifierNode, Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?): LiteColumnChildData {
        return ((parentData as? LiteColumnChildData) ?: LiteColumnChildData()).also {
            it.alignment = alignment
        }
    }
}

@Immutable
private class LiteColumnChildDataElement(
    val alignment: HorizontalAlignment
) : ModifierNodeElement<LiteColumnChildDataAlignment>() {
    override fun create(): LiteColumnChildDataAlignment {
        return LiteColumnChildDataAlignment(alignment = alignment)
    }

    override fun update(node: LiteColumnChildDataAlignment) {
        node.alignment = alignment
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? LiteColumnChildDataElement ?: return false
        return alignment == otherModifier.alignment
    }

    override fun hashCode(): Int = alignment.hashCode() * 31
}

@Immutable
class LiteColumnChildDataNodeWeight(
    var weight: Float = 0f
) : ParentDataModifierNode, Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?): LiteColumnChildData {
        return ((parentData as? LiteColumnChildData) ?: LiteColumnChildData()).also {
            it.weight = weight
        }
    }
}

@Immutable
private class LiteColumnWeightElement(
    val weight: Float
) : ModifierNodeElement<LiteColumnChildDataNodeWeight>() {
    override fun create(): LiteColumnChildDataNodeWeight {
        return LiteColumnChildDataNodeWeight(weight = weight)
    }

    override fun update(node: LiteColumnChildDataNodeWeight) {
        node.weight = weight
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? LiteColumnWeightElement ?: return false
        return weight == otherModifier.weight
    }

    override fun hashCode(): Int = weight.hashCode() * 31
}

@Stable
val Measurable.liteColumnChildDataNode: LiteColumnChildData? get() = parentData as? LiteColumnChildData