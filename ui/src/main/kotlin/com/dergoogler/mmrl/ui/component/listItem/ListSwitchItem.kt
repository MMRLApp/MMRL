package com.dergoogler.mmrl.ui.component.listItem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun ListSwitchItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp),
    itemTextStyle: ListItemTextStyle = ListItemDefaults.itemStyle,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    @DrawableRes icon: Int? = null,
    enabled: Boolean = true,
    base: BaseParameters.() -> Unit = {},
) {
    val layoutDirection = LocalLayoutDirection.current
    val start by remember {
        derivedStateOf { contentPaddingValues.calculateStartPadding(layoutDirection) }
    }

    Row(
        modifier =
            modifier
                .alpha(alpha = if (enabled) 1f else 0.5f)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    onValueChange = onChange,
                    role = Role.Switch,
                    interactionSource = interactionSource,
                    indication = ripple(),
                ).padding(contentPaddingValues)
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                modifier = Modifier.size(itemTextStyle.iconSize),
                painter = painterResource(id = icon),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(start))
        }

        BaseListContent(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = itemTextStyle.textSwitchPadding),
            title = title,
            desc = desc,
            itemTextStyle = itemTextStyle,
            base = base,
        )

        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
}
