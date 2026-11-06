package com.dergoogler.mmrl.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ui.providable.LocalNavController
import dev.mmrlx.compose.ui.list.List
import dev.mmrlx.compose.ui.list.ListScope
import dev.mmrlx.compose.ui.scaffold.Scaffold
import dev.mmrlx.compose.ui.toolbar.NavigateUpToolbar
import dev.mmrlx.compose.ui.toolbar.Toolbar
import dev.mmrlx.compose.ui.toolbar.ToolbarDefaults
import dev.mmrlx.compose.ui.toolbar.ToolbarTitle

@Composable
fun SettingsScaffold(
    @StringRes title: Int,
    isMainPage: Boolean = false,
    canScroll: Boolean = true,
    actions: @Composable (RowScope.() -> Unit) = {},
    floatingActionButton: @Composable () -> Unit = {},
    relative: @Composable (ListScope.() -> Unit),
) = SettingsScaffold(
    title = stringResource(id = title),
    actions = actions,
    isMainPage = isMainPage,
    canScroll = canScroll,
    floatingActionButton = floatingActionButton,
    relative = relative,
)

@Composable
fun SettingsScaffold(
    title: String,
    isMainPage: Boolean = false,
    canScroll: Boolean = true,
    actions: @Composable (RowScope.() -> Unit) = {},
    floatingActionButton: @Composable () -> Unit = {},
    relative: @Composable (ListScope.() -> Unit),
) {
    val navController = LocalNavController.current
    val scrollBehavior = ToolbarDefaults.fadePinnedScrollBehavior()

    val scrollModifier = if (canScroll) {
        Modifier.verticalScroll(rememberScrollState())
    } else Modifier

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        toolbar = {
            if (!isMainPage) {
                NavigateUpToolbar(
                    title = title,
                    navController = navController,
                    actions = actions,
                    scrollBehavior = scrollBehavior,
                )
            } else {
                Toolbar(
                    title = {
                        ToolbarTitle(title = title)
                    },
                    actions = actions,
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        bottomBar = {
            if (isMainPage) {
                BottomBar()
            }
        },
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets.none,
    ) {
        List(
            modifier = Modifier
                .then(scrollModifier)
                .scaffoldHazeSource()
                .scaffoldPadding(),
            content = {
                relative()
            },
        )
    }
}

@Immutable
class ScaffoldModifier internal constructor(
    val box: Modifier,
    val column: Modifier,
) {
    @Suppress("RedundantIf")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ScaffoldModifier) return false

        if (box != other.box) return false
        if (column != other.column) return false

        return true
    }

    fun copy(
        box: Modifier = this.box,
        column: Modifier = this.column,
    ): ScaffoldModifier =
        ScaffoldModifier(
            box,
            column,
        )

    override fun hashCode(): Int {
        var result = box.hashCode()
        result = 31 * result + column.hashCode()
        return result
    }
}

object ScaffoldDefaults {
    val settingsScaffoldModifier
        get() =
            ScaffoldModifier(
                box = Modifier.fillMaxSize(),
                column = Modifier.fillMaxSize(),
            )

    val settingsScaffoldScrollModifier
        @Composable get() =
            ScaffoldModifier(
                box = Modifier.fillMaxSize(),
                column =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
            )
}
