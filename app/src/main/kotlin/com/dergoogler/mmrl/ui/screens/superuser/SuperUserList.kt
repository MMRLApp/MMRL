package com.dergoogler.mmrl.ui.screens.superuser

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.scaffold.ScaffoldScope
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.viewmodel.SuperUserViewModel
import com.ramcosta.composedestinations.generated.destinations.AppProfileScreenDestination
import dev.chrisbanes.haze.hazeSource

@Composable
fun ScaffoldScope.SuperUserList(
    innerPadding: PaddingValues,
    list: List<SuperUserViewModel.AppInfo>,
    state: LazyListState,
) {
    val navigator = LocalDestinationsNavigator.current
    val paddingValues = LocalMainScreenInnerPaddings.current
    val layoutDirection = LocalLayoutDirection.current

    this@SuperUserList.ResponsiveContent {
        List {
            LazyColumn(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = LocalHazeState.current),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    bottom = paddingValues.calculateBottomPadding(),
                    end = 0.dp
                ),
            ) {
                items(
                    items = list,
                    key = { it.packageName + it.uid }
                ) { app ->
                    SuperUserItem(app) {
                        navigator.navigate(AppProfileScreenDestination(app))
                    }
                }
            }
        }
    }
}