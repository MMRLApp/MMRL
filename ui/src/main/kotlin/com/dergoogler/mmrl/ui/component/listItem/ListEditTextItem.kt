package com.dergoogler.mmrl.ui.component.listItem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.component.dialog.DialogParameters
import com.dergoogler.mmrl.ui.component.dialog.EditTextDialog

@Composable
fun ListEditTextItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    value: String,
    onConfirm: (String) -> Unit,
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp),
    itemTextStyle: ListItemTextStyle = ListItemDefaults.itemStyle,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    onValid: ((String) -> Boolean)? = null,
    dialog: DialogParameters.() -> Unit = {},
    base: BaseParameters.() -> Unit = {},
) {
    val dialogParameters = remember { DialogParameters() }.apply(dialog)

    var open by remember { mutableStateOf(false) }
    if (open) {
        EditTextDialog(
            value = value,
            title = title,
            onClose = { open = false },
            onConfirm = onConfirm,
            onValid = onValid,
            dialogParameters = dialogParameters,
        )
    }

    ListButtonItem(
        modifier = modifier,
        icon = icon,
        title = title,
        desc = desc,
        onClick = { open = true },
        contentPaddingValues = contentPaddingValues,
        interactionSource = interactionSource,
        enabled = enabled,
        itemTextStyle = itemTextStyle,
        base = base,
    )
}
