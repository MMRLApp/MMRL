@file:Suppress("PropertyName")

package com.dergoogler.mmrl.ui.component.dialog.dsl

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.token.DialogContainerTokens
import com.dergoogler.mmrl.ui.token.value

enum class DialogContainerSlot {
    TITLE, CONTENT, BUTTONS
}

@LayoutScopeMarker
@Immutable
interface DialogContainerScope {
    val style: DialogContainerStyle
    val contentPadding: DialogContentPadding
}

@Composable
infix fun TextStyle.Provide(content: @Composable () -> Unit) {
    ProvideTextStyle(this, content)
}

internal class DialogContainerScopeInstance(
    override val style: DialogContainerStyle,
    override val contentPadding: DialogContentPadding
) : DialogContainerScope

data class DialogContainerStyle(
    val borderWidth: Dp,
    val shape: Shape,
    val containerOutline: Color,
    val containerColor: Color,
    val iconContentColor: Color,
    val titleContentColor: Color,
    val textContentColor: Color,
    val tonalElevation: Dp,
)

data class DialogContentPadding(
    val title: PaddingValues,
    val content: PaddingValues,
    val buttons: PaddingValues
) {
    val EMPTY_CONTENT get() = this.copy(content = PaddingValues(0.dp))
}

object DialogContainerDefaults {
    val contentPadding = DialogContentPadding(
        title = PaddingValues(
            top = 25.dp,
            bottom = 16.dp,
            start = 25.dp,
            end = 25.dp
        ),
        content = PaddingValues(
            vertical = 0.dp,
            horizontal = 25.dp
        ),
        buttons = PaddingValues(
            vertical = 16.dp,
            horizontal = 25.dp
        )
    )

    val style
        @Composable get() = DialogContainerStyle(
            borderWidth = Dp.Hairline,
            shape = DialogContainerTokens.ContainerShape.value,
            containerOutline = DialogContainerTokens.ContainerOutline.value,
            containerColor = DialogContainerTokens.ContainerColor.value,
            iconContentColor = DialogContainerTokens.IconColor.value,
            titleContentColor = DialogContainerTokens.HeadlineColor.value,
            textContentColor = DialogContainerTokens.SupportingTextColor.value,
            tonalElevation = 0.dp
        )
}