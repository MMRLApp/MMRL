package com.dergoogler.mmrl.ui.component.listItem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListButtonItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp),
    itemTextStyle: ListItemTextStyle = ListItemDefaults.itemStyle,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    @DrawableRes icon: Int? = null,
    iconToRight: Boolean = false,
    enabled: Boolean = true,
    base: BaseParameters.() -> Unit = {},
) {
    val layoutDirection = LocalLayoutDirection.current
    val start by remember {
        derivedStateOf {
            if (!iconToRight) {
                contentPaddingValues.calculateStartPadding(
                    layoutDirection,
                )
            } else {
                contentPaddingValues.calculateEndPadding(layoutDirection)
            }
        }
    }

    Row(
        modifier =
            modifier
                .alpha(alpha = if (enabled) 1f else 0.5f)
                .combinedClickable(
                    enabled = enabled,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    interactionSource = interactionSource,
                    indication = ripple(),
                ).padding(contentPaddingValues)
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!iconToRight) {
            icon?.let {
                Icon(
                    modifier = Modifier.size(itemTextStyle.iconSize),
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = LocalContentColor.current,
                )

                Spacer(modifier = Modifier.width(start))
            }
        }

        BaseListContent(
            modifier = Modifier.weight(1f),
            title = title,
            desc = desc,
            itemTextStyle = itemTextStyle,
            base = base,
        )

        if (iconToRight) {
            Spacer(modifier = Modifier.width(start))
            icon?.let {
                Icon(
                    modifier = Modifier.size(itemTextStyle.iconSize),
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = LocalContentColor.current,
                )
            }
        }
    }
}
