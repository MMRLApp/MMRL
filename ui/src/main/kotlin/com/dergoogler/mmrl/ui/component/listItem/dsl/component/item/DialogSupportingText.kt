package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ui.component.listItem.dsl.DialogItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlotScope
import com.dergoogler.mmrl.ui.component.text.BBCodeText

@Composable
fun ListItemScope.DialogSupportingText(
    content: @Composable ListItemSlotScope.() -> Unit,
) = Slot(
    slot = DialogItemSlot.SupportingText,
    content = content
)

@Composable
fun ListItemScope.DialogSupportingText(text: String) {
    this.DialogSupportingText {
        BBCodeText(text)
    }
}

@Composable
fun ListItemScope.DialogSupportingText(
    @StringRes id: Int,
) = this.DialogSupportingText(stringResource(id))

@Composable
fun ListItemScope.DialogSupportingText(
    @StringRes id: Int,
    vararg formatArgs: Any,
) = this.DialogSupportingText(stringResource(id, *formatArgs))
