package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope

/**
 * A list item that behaves like a button.
 *
 * @param onClick The callback to be invoked when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param interactionSource The [MutableInteractionSource] representing the stream of
 *   [Interaction]s for this button. You can create and pass in your own remembered
 *   [MutableInteractionSource] if you want to observe [Interaction]s and customize the
 *   appearance / behavior of this button in different [Interaction]s.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be
 *   clickable.
 * @param content The content of the button.
 */
@Composable
fun ListScope.ButtonItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    contentPadding: PaddingValues = contentPaddingValues,
    content: @Composable ListItemScope.() -> Unit,
) {
    this.Item(
        contentPadding = contentPadding,
        modifier =
            modifier
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    role = Role.Button,
                    indication = ripple(),
                    onClick = onClick,
                ),
        enabled = enabled,
        content = content,
    )
}
