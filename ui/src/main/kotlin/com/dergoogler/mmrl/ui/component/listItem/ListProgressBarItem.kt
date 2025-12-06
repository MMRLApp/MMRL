package com.dergoogler.mmrl.ui.component.listItem

import androidx.annotation.DrawableRes
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.thenCompose

@Composable
fun ListProgressBarItem(
    modifier: Modifier = Modifier,
    title: String,
    startDesc: String? = null,
    endDesc: String? = null,
    progress: Float,
    progressBarHeight: Dp = 10.dp,
    progressBarModifier: Modifier = Modifier,
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
            desc = {
                Row(
                    modifier =
                        Modifier
                            .padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    startDesc.thenCompose<String> {
                        Text(
                            text = it,
                            style = itemTextStyle.descTextStyle,
                            color = itemTextStyle.descTextColor,
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    LinearProgressIndicator(
                        progress = {
                            progress
                        },
                        modifier =
                            Modifier
                                .height(progressBarHeight)
                                .then(progressBarModifier),
                        drawStopIndicator = {},
                    )

                    endDesc.thenCompose<String> {
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = it,
                            style = itemTextStyle.descTextStyle,
                            color = itemTextStyle.descTextColor,
                        )
                    }
                }
            },
            itemTextStyle = itemTextStyle,
            base = base,
        )
    }
}
