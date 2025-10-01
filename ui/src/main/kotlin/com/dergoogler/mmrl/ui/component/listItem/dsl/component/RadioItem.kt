package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.End

@Composable
fun ListScope.RadioItem(
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ListItemScope.() -> Unit,
) {
    Item(
        enabled = enabled,
        modifier = Modifier.selectable(
            selected = selected,
            enabled = enabled,
            onClick = onClick,
            role = Role.RadioButton,
            interactionSource = interactionSource,
            indication = ripple()
        )
    ) {
        content()
        End {
            RadioButton(
                selected = selected,
                onClick = null
            )
        }
    }
}