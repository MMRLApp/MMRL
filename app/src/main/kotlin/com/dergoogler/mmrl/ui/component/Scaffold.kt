package com.dergoogler.mmrl.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.systemBarsPaddingEnd
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.scaffold.ResponsiveScaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurNavigateUpToolbar
import com.dergoogler.mmrl.ui.component.toolbar.BlurToolbar
import com.dergoogler.mmrl.ui.component.toolbar.ToolbarTitle
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalNavController
import dev.chrisbanes.haze.hazeSource

@Composable
fun SettingsScaffold(
    modifier: ScaffoldModifier = ScaffoldDefaults.settingsScaffoldScrollModifier,
    @StringRes title: Int,
    allowNavigateBack: Boolean = true,
    actions: @Composable (RowScope.() -> Unit) = {},
    floatingActionButton: @Composable () -> Unit = {},
    absolute: @Composable (BoxScope.() -> Unit) = {},
    relative: @Composable (ListScope.() -> Unit),
) = SettingsScaffold(
    title = stringResource(id = title),
    modifier = modifier,
    actions = actions,
    allowNavigateBack = allowNavigateBack,
    floatingActionButton = floatingActionButton,
    absolute = absolute,
    relative = relative
)

@Composable
fun SettingsScaffold(
    modifier: ScaffoldModifier = ScaffoldDefaults.settingsScaffoldScrollModifier,
    title: String,
    allowNavigateBack: Boolean = true,
    actions: @Composable (RowScope.() -> Unit) = {},
    floatingActionButton: @Composable () -> Unit = {},
    absolute: @Composable (BoxScope.() -> Unit) = {},
    relative: @Composable (ListScope.() -> Unit),
) = LocalScreenProvider {
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    ResponsiveScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (allowNavigateBack) {
                BlurNavigateUpToolbar(
                    title = title,
                    navController = navController,
                    actions = actions,
                    fade = true,
                    fadeDistance = 50f,
                    scrollBehavior = scrollBehavior
                )
            } else {
                BlurToolbar(
                    title = {
                        ToolbarTitle(title = title)
                    },
                    fade = true,
                    fadeDistance = 50f,
                    actions = actions,
                    scrollBehavior = scrollBehavior
                )
            }
        },
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .hazeSource(state = LocalHazeState.current)
                .then(modifier.box)
        ) {
            List(
                modifier = modifier.column
                    .systemBarsPaddingEnd()
                    .padding(top = innerPadding.calculateTopPadding()),
                content = {
                    relative()

                    val paddingValues = LocalMainScreenInnerPaddings.current
                    Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                }
            )

            absolute()
        }
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
    ): ScaffoldModifier = ScaffoldModifier(
        box, column
    )

    override fun hashCode(): Int {
        var result = box.hashCode()
        result = 31 * result + column.hashCode()
        return result
    }
}

object ScaffoldDefaults {
    val settingsScaffoldModifier
        get() = ScaffoldModifier(
            box = Modifier.fillMaxSize(),
            column = Modifier.fillMaxSize()
        )

    val settingsScaffoldScrollModifier
        @Composable get() = ScaffoldModifier(
            box = Modifier.fillMaxSize(),
            column = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        )
}