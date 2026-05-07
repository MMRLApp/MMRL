package com.dergoogler.mmrl.ui.component.listItem

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.thenComposeInvoke
import com.dergoogler.mmrl.ui.R

data class BaseParameters(
    @StringRes var learnMoreText: Int = R.string.learn_more,
    var learnMore: (() -> Unit)? = null,
    var labels: List<@Composable RowScope.() -> Unit>? = null,
)

@Composable
internal fun BaseListContent(
    modifier: Modifier = Modifier,
    title: String,
    desc: (@Composable ColumnScope.() -> Unit)? = null,
    itemTextStyle: ListItemTextStyle = ListItemDefaults.itemStyle,
    base: BaseParameters.() -> Unit = {},
) {
    val baseParameters = remember { BaseParameters() }.apply(base)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = itemTextStyle.titleTextStyle,
            color = itemTextStyle.titleTextColor,
        )
        desc?.let {
            it()
        }
        baseParameters.labels.nullable {
            Row(
                modifier = Modifier.padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                it.forEach { label ->
                    label()
                }
            }
        }
    }
}

@Composable
internal fun BaseListContent(
    modifier: Modifier = Modifier,
    title: String,
    desc: String? = null,
    itemTextStyle: ListItemTextStyle = ListItemDefaults.itemStyle,
    base: BaseParameters.() -> Unit = {},
) {
    val baseParameters = remember { BaseParameters() }.apply(base)

    BaseListContent(
        modifier = modifier,
        title = title,
        base = base,
        desc =
            desc.thenComposeInvoke<String, ColumnScope> { s ->
                Text(
                    text = s,
                    style = itemTextStyle.descTextStyle,
                    color = itemTextStyle.descTextColor,
                )
                baseParameters.learnMore.nullable {
                    Text(
                        modifier =
                            Modifier.clickable(
                                onClick = it,
                            ),
                        text = stringResource(baseParameters.learnMoreText),
                        style = itemTextStyle.descTextStyle,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
        itemTextStyle = itemTextStyle,
    )
}
