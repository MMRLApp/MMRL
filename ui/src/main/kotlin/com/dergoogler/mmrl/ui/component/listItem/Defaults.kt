package com.dergoogler.mmrl.ui.component.listItem

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
class ListItemTextStyle(
    val titleTextColor: Color,
    val descTextColor: Color,
    val titleTextStyle: TextStyle,
    val descTextStyle: TextStyle,
    val iconSize: Dp = 24.dp,
    val textSwitchPadding: Dp = 16.dp,
) {
    @Suppress("RedundantIf")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ListItemTextStyle) return false

        if (titleTextColor != other.titleTextColor) return false
        if (descTextColor != other.descTextColor) return false
        if (titleTextStyle != other.titleTextStyle) return false
        if (descTextStyle != other.descTextStyle) return false
        if (iconSize != other.iconSize) return false
        if (textSwitchPadding != other.textSwitchPadding) return false

        return true
    }

    fun copy(
        titleTextColor: Color = this.titleTextColor,
        descTextColor: Color = this.descTextColor,
        titleTextStyle: TextStyle = this.titleTextStyle,
        descTextStyle: TextStyle = this.descTextStyle,
        iconSize: Dp = this.iconSize,
        textSwitchPadding: Dp = this.textSwitchPadding,
    ): ListItemTextStyle =
        ListItemTextStyle(
            titleTextColor,
            descTextColor,
            titleTextStyle,
            descTextStyle,
            iconSize,
            textSwitchPadding,
        )

    override fun hashCode(): Int {
        var result = titleTextColor.hashCode()
        result = 31 * result + descTextColor.hashCode()
        result = 31 * result + titleTextStyle.hashCode()
        result = 31 * result + descTextStyle.hashCode()
        result = 31 * result + iconSize.hashCode()
        result = 31 * result + textSwitchPadding.hashCode()
        return result
    }
}

data class ListItemModifiers(
    val title: Modifier,
    val desc: Modifier,
    val icon: Modifier,
)

object ListItemDefaults {
    val itemStyle
        @Composable get() =
            ListItemTextStyle(
                titleTextColor = LocalContentColor.current,
                descTextColor = MaterialTheme.colorScheme.outline,
                titleTextStyle = MaterialTheme.typography.bodyLarge,
                descTextStyle = MaterialTheme.typography.bodyMedium,
                iconSize = 24.dp,
                textSwitchPadding = 16.dp,
            )

    val modifiers get() =
        ListItemModifiers(
            title = Modifier,
            desc = Modifier,
            icon = Modifier,
        )
}
