package com.dergoogler.mmrl.ext

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Deprecated(message = "Use ignoreParentPadding instead")
fun Modifier.ignoreHorizontalParentPadding(horizontal: Dp): Modifier {
    return this.layout { measurable, constraints ->
        val overridenWidth = constraints.maxWidth + 2 * horizontal.roundToPx()
        val placeable = measurable.measure(constraints.copy(maxWidth = overridenWidth))
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

fun Modifier.ignoreParentPadding(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
    start: Dp = 0.dp,
    end: Dp = 0.dp,
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
): Modifier {
    return this.layout { measurable, constraints ->
        // Combine individual and symmetrical paddings
        val totalStart = start.roundToPx() + horizontal.roundToPx()
        val totalEnd = end.roundToPx() + horizontal.roundToPx()
        val totalTop = top.roundToPx() + vertical.roundToPx()
        val totalBottom = bottom.roundToPx() + vertical.roundToPx()

        val horizontalPadding = totalStart + totalEnd
        val verticalPadding = totalTop + totalBottom

        // Adjust constraints by adding the calculated padding values
        val overriddenWidth = constraints.maxWidth + horizontalPadding
        val overriddenHeight = constraints.maxHeight + verticalPadding

        // Measure with updated constraints
        val placeable = measurable.measure(
            constraints.copy(
                maxWidth = overriddenWidth,
                maxHeight = overriddenHeight
            )
        )

        // Layout with adjusted size
        layout(placeable.width, placeable.height) {
            placeable.place(-totalStart, -totalTop)
        }
    }
}

fun Modifier.fillWidthOfParent(parentPadding: Dp) = this.layout { measurable, constraints ->
    val placeable =
        measurable.measure(constraints.copy(maxWidth = constraints.maxWidth + 2 * parentPadding.roundToPx()))
    layout(placeable.width, placeable.height) {
        placeable.place(0, 0)
    }
}

fun Modifier.fillSizeOfParent(
    all: Dp = Dp.Unspecified,
    horizontalPadding: Dp = 0.dp,
    verticalPadding: Dp = 0.dp,
) = this.layout { measurable, constraints ->
    val resolvedHorizontal = if (all != Dp.Unspecified) all else horizontalPadding
    val resolvedVertical = if (all != Dp.Unspecified) all else verticalPadding

    val extraWidth = 2 * resolvedHorizontal.roundToPx()
    val extraHeight = 2 * resolvedVertical.roundToPx()

    val overriddenWidth = (constraints.maxWidth + extraWidth).coerceAtLeast(0)
    val overriddenHeight = (constraints.maxHeight + extraHeight).coerceAtLeast(0)

    val placeable = measurable.measure(
        constraints.copy(
            maxWidth = overriddenWidth,
            maxHeight = overriddenHeight
        )
    )

    layout(placeable.width, placeable.height) {
        placeable.place(-extraWidth / 2, -extraHeight / 2)
    }
}

fun Modifier.systemBarsPaddingEnd() = composed {
    val layoutDirection = LocalLayoutDirection.current
    return@composed padding(
        end = WindowInsets.systemBars
            .asPaddingValues()
            .calculateEndPadding(layoutDirection),
    )
}

fun Modifier.fadingEdge(
    brush: Brush,
    height: Float? = null,
    blendMode: BlendMode = BlendMode.DstIn,
) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val gradientHeight = if (height != null) size.height * height else size.height
        drawRect(
            brush = brush,
            size = size.copy(height = gradientHeight),
            blendMode = blendMode
        )
    }

fun Modifier.applyAlpha(enabled: Boolean): Modifier = this.alpha(if (enabled) 1f else 0.5f)

fun Modifier.iconSize(
    density: Density,
    textStyle: TextStyle,
    scaling: Float,
): Modifier {
    val iconSize = with(density) { textStyle.fontSize.toDp() * scaling }
    return this.size(iconSize)
}

fun Modifier.onClick(
    role: Role = Role.Button,
    enabled: Boolean = true,
    click: () -> Unit,
) = composed {
    val interactionSource = remember { MutableInteractionSource() }

    clickable(
        interactionSource = interactionSource,
        enabled = enabled,
        onClick = click,
        role = role,
        indication = ripple()
    )
}

interface ModifierScope {
    var surface: Modifier
    var box: Modifier
    var column: Modifier

    override fun equals(other: Any?): Boolean

    fun copy(
        surface: Modifier = this.surface,
        box: Modifier = this.box,
        column: Modifier = this.column,
    ): ModifierScope

    fun then(
        surface: Modifier = this.surface,
        box: Modifier = this.box,
        column: Modifier = this.column,
    ): ModifierScope

    fun copy(
        original: ModifierScope,
    ): ModifierScope

    override fun hashCode(): Int
}

typealias ModifierScopeUnit = @Composable (ModifierScope.() -> Unit)

@Immutable
class ModifierScopeImpl(
    override var surface: Modifier,
    override var box: Modifier,
    override var column: Modifier,
) : ModifierScope {
    constructor(original: ModifierScope) : this(
        surface = original.surface,
        box = original.box,
        column = original.column
    )

    @Suppress("RedundantIf")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ModifierScope) return false

        if (box != other.box) return false
        if (column != other.column) return false
        if (surface != other.surface) return false

        return true
    }

    override fun copy(
        surface: Modifier,
        box: Modifier,
        column: Modifier,
    ): ModifierScope = ModifierScopeImpl(
        surface = surface,
        box = box,
        column = column
    )

    override fun then(
        surface: Modifier,
        box: Modifier,
        column: Modifier,
    ): ModifierScope = ModifierScopeImpl(
        surface = this.surface.then(surface),
        box = this.box.then(box),
        column = this.column.then(column)
    )

    override fun copy(
        original: ModifierScope,
    ): ModifierScope = ModifierScopeImpl(
        surface = original.surface,
        box = original.box,
        column = original.column
    )

    override fun hashCode(): Int {
        var result = box.hashCode()
        result = 31 * result + column.hashCode()
        result = 31 * result + surface.hashCode()
        return result
    }
}