package com.dergoogler.mmrl.ui.component.listItem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    contentPaddingValues: PaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp),
    itemTextStyle: ListItemTextStyle = ListItemDefaults.itemStyle,
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
                .padding(contentPaddingValues)
                .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                modifier = Modifier.size(itemTextStyle.iconSize),
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = LocalContentColor.current,
            )

            Spacer(modifier = Modifier.width(start))
        }

        BaseListContent(
            title = title,
            desc = desc,
            itemTextStyle = itemTextStyle,
            base = base,
        )
    }
}
