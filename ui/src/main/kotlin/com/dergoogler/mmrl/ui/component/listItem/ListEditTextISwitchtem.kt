package com.dergoogler.mmrl.ui.component.listItem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.component.dialog.DialogParameters
import com.dergoogler.mmrl.ui.component.dialog.EditTextDialog

@Composable
fun ListEditTextSwitchItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    value: String,
    onConfirm: (String) -> Unit,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
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
    val layoutDirection = LocalLayoutDirection.current
    val start by remember {
        derivedStateOf { contentPaddingValues.calculateStartPadding(layoutDirection) }
    }

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

    val alpha = remember(enabled) { if (enabled) Modifier.alpha(1f) else Modifier.alpha(0.38f) }

    Row(
        modifier =
            modifier
                .let {
                    if (checked) {
                        it.clickable(
                            enabled = enabled,
                            onClick = { open = true },
                            interactionSource = interactionSource,
                            indication = ripple(),
                        )
                    } else {
                        Modifier
                    }
                }.padding(contentPaddingValues)
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                modifier =
                    Modifier
                        .size(itemTextStyle.iconSize)
                        .then(alpha),
                painter = painterResource(id = icon),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(start))
        }

        BaseListContent(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = itemTextStyle.textSwitchPadding)
                    .then(alpha),
            title = title,
            desc = desc,
            itemTextStyle = itemTextStyle,
            base = base,
        )

        VerticalDivider(
            modifier = Modifier.padding(16.dp),
            thickness = 0.9.dp,
        )

        Switch(
            enabled = enabled,
            checked = checked,
            onCheckedChange = onChange,
        )
    }
}
