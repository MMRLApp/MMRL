package com.dergoogler.mmrl.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.R

@Deprecated(
    "Use ToolbarTitle instead",
    replaceWith = ReplaceWith("com.dergoogler.mmrl.ui.component.toolbar.ToolbarTitle(text = text, modifier = modifier)"),
)
@Composable
fun TopAppBarTitle(
    text: String,
    modifier: Modifier = Modifier,
) = Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = LocalContentColor.current,
    )
}

@Deprecated(
    "Use ToolbarIcon instead",
    replaceWith = ReplaceWith("com.dergoogler.mmrl.ui.component.toolbar.ToolbarIcon(icon = icon, modifier = modifier)"),
)
@Composable
fun TopAppBarIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.mmrl_logo,
    tint: Color = MaterialTheme.colorScheme.surfaceTint,
) = Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
) {
    Icon(
        modifier = Modifier.size(30.dp),
        painter = painterResource(id = icon),
        contentDescription = null,
        tint = tint,
    )
}

@ExperimentalMaterial3Api
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) = androidx.compose.material3.TopAppBar(
    title = title,
    modifier =
        Modifier
            .clip(
                RoundedCornerShape(
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                ),
            ).then(modifier),
    navigationIcon = navigationIcon,
    actions = actions,
    colors = colors,
    scrollBehavior = scrollBehavior,
    windowInsets = windowInsets,
    expandedHeight = expandedHeight,
)

@ExperimentalMaterial3Api
@Composable
fun CenterAlignedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) = androidx.compose.material3.CenterAlignedTopAppBar(
    title = title,
    modifier =
        Modifier
            .clip(
                RoundedCornerShape(
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp,
                ),
            ).then(modifier),
    navigationIcon = navigationIcon,
    actions = actions,
    colors = colors,
    scrollBehavior = scrollBehavior,
    windowInsets = windowInsets,
    expandedHeight = expandedHeight,
)
