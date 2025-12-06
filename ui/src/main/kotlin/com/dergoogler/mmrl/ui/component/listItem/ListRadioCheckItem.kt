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
import com.dergoogler.mmrl.ui.component.dialog.RadioCheckDialog
import com.dergoogler.mmrl.ui.component.dialog.RadioOptionItem

@Composable
fun <T> ListRadioCheckItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    value: T,
    options: List<RadioOptionItem<T>>,
    suffix: String? = null,
    prefix: String? = null,
    onConfirm: (RadioOptionItem<T>) -> Unit,
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp),
    itemTextStyle: ListItemTextStyle = ListItemDefaults.itemStyle,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    base: BaseParameters.() -> Unit = {},
) {
    var open by remember { mutableStateOf(false) }
    if (open) {
        RadioCheckDialog(
            value = value,
            title = title,
            suffix = suffix,
            prefix = prefix,
            options = options,
            onClose = { open = false },
            onConfirm = onConfirm,
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
