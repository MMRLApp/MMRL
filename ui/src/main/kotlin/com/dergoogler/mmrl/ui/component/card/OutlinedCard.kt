package com.dergoogler.mmrl.ui.component.card

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.ext.ModifierScopeUnit

@Composable
fun OutlinedCard(
    modifier: ModifierScopeUnit = {},
    style: CardStyle = CardDefaults.outlinedCardStyle,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    absolute: @Composable (BoxScope.() -> Unit) = {},
    relative: @Composable (ColumnScope.() -> Unit),
) = BaseCard(
    modifier = modifier,
    modifierScope = CardDefaults.outlinedCardModifier,
    style = style,
    enabled = enabled,
    onClick = onClick,
    onLongClick = onLongClick,
    interactionSource = interactionSource,
    absolute = absolute,
    relative = relative,
)
