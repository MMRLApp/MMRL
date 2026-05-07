package com.dergoogler.mmrl.ui.component.toolbar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.R

@Composable
fun ToolbarIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.mmrl_logo,
    subtitle: String? = null,
    tint: Color = MaterialTheme.colorScheme.surfaceTint,
) = ToolbarTitleLayout(
    modifier = modifier,
    title = {
        Icon(
            modifier = Modifier.size(30.dp),
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = tint,
        )
    },
    subtitle =
        subtitle.nullable {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = LocalContentColor.current.copy(alpha = 0.75f),
                )
            }
        },
)
