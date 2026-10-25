package dev.mmrlx.compose.ui.toolbar

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.icon.IconButton
import dev.mmrlx.ui.R

@Composable
fun NavigateUpToolbar(
    title: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enable: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = ToolbarDefaults.windowInsets,
    scrollBehavior: ToolbarScrollBehavior? = null,
) = NavigateUpToolbar(
    modifier = modifier,
    title = title,
    subtitle = subtitle,
    onBack = { navController.popBackStack() },
    actions = actions,
    windowInsets = windowInsets,
    scrollBehavior = scrollBehavior,
    enable = enable,
)

@Composable
fun NavigateUpToolbar(
    title: String,
    navigator: DestinationsNavigator,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enable: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = ToolbarDefaults.windowInsets,
    scrollBehavior: ToolbarScrollBehavior? = null,
) = NavigateUpToolbar(
    modifier = modifier,
    title = title,
    subtitle = subtitle,
    onBack = { navigator.popBackStack() },
    actions = actions,
    windowInsets = windowInsets,
    scrollBehavior = scrollBehavior,
    enable = enable,
)

@Composable
fun NavigateUpToolbar(
    title: String,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    subtitle: String? = null,
    enable: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = ToolbarDefaults.windowInsets,
    scrollBehavior: ToolbarScrollBehavior? = null,
) = NavigateUpToolbar(
    modifier = modifier,
    title = title,
    subtitle = subtitle,
    onBack = { (context as ComponentActivity).finish() },
    actions = actions,
    windowInsets = windowInsets,
    scrollBehavior = scrollBehavior,
    enable = enable,
)

@Composable
fun NavigateUpToolbar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enable: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = ToolbarDefaults.windowInsets,
    scrollBehavior: ToolbarScrollBehavior? = null,
) = NavigateUpToolbar(
    modifier = modifier,
    title = {
        ToolbarTitle(title = title, subtitle = subtitle)
    },
    onBack = onBack,
    actions = actions,
    windowInsets = windowInsets,
    scrollBehavior = scrollBehavior,
    enable = enable,
)

@Composable
fun NavigateUpToolbar(
    title: @Composable () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = ToolbarDefaults.windowInsets,
    scrollBehavior: ToolbarScrollBehavior? = null,
) = Toolbar(
    title = title,
    modifier = modifier,
    navigationIcon = {
        if (enable) {
            IconButton(
                onClick = { if (enable) onBack() },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = null,
                )
            }
        }
    },
    actions = actions,
    windowInsets = windowInsets,
    scrollBehavior = scrollBehavior,
)
