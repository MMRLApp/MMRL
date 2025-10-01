package com.dergoogler.mmrl.ui.screens.repositories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.state.RepoState
import com.dergoogler.mmrl.ui.component.scaffold.ScaffoldScope
import com.dergoogler.mmrl.ui.component.scrollbar.VerticalFastScrollbar
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.screens.repositories.items.ExploreReposCard
import com.ramcosta.composedestinations.generated.destinations.RepositoryScreenDestination
import dev.chrisbanes.haze.hazeSource

@Composable
fun ScaffoldScope.RepositoriesList(
    list: List<RepoState>,
    state: LazyListState,
    delete: (RepoState) -> Unit,
    getUpdate: (RepoState, (Throwable) -> Unit) -> Unit,
    innerPadding: PaddingValues,
) = Box(
    modifier = Modifier.fillMaxSize()
) {
    val paddingValues = LocalMainScreenInnerPaddings.current
    val layoutDirection = LocalLayoutDirection.current
    val navigator = LocalDestinationsNavigator.current

    this@RepositoriesList.ResponsiveContent {
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = LocalHazeState.current),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
                bottom = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                ExploreReposCard()
            }

            items(
                items = list,
                key = { it.url }
            ) { repo ->
                RepositoryItem(
                    repo = repo,
                    onClick = {
                        navigator.navigate(RepositoryScreenDestination(repo.toRepo()))
                    },
                    onUpdate = getUpdate,
                    onDelete = delete,
                )
            }

            item {
                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
            }
        }
    }

    VerticalFastScrollbar(
        state = state,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(
                top = innerPadding.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            )
    )
}

@Composable
private fun RepositoryItem(
    repo: RepoState,
    onClick: () -> Unit,
    onUpdate: (RepoState, (Throwable) -> Unit) -> Unit,
    onDelete: (RepoState) -> Unit,
) {
    var delete by remember { mutableStateOf(false) }
    if (delete) DeleteDialog(
        repo = repo,
        onClose = { delete = false },
        onConfirm = { onDelete(repo) }
    )

    var failure by remember { mutableStateOf(false) }
    var message: String by remember { mutableStateOf("") }
    if (failure) FailureDialog(
        name = repo.name,
        message = message,
        onClose = {
            failure = false
            message = ""
        }
    )

    RepositoryItem(
        repo = repo,
        onClick = onClick,
        update = {
            onUpdate(repo) {
                failure = true
                message = it.stackTraceToString()
            }
        },
        delete = { delete = true }
    )
}

@Composable
private fun DeleteDialog(
    repo: RepoState,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) = AlertDialog(
    shape = RoundedCornerShape(20.dp),
    onDismissRequest = onClose,
    title = { Text(text = stringResource(id = R.string.dialog_attention)) },
    text = {
        Text(text = stringResource(id = R.string.repo_delete_dialog_desc, repo.name))
    },
    confirmButton = {
        TextButton(
            onClick = {
                onConfirm()
                onClose()
            }
        ) {
            Text(text = stringResource(id = R.string.repo_options_delete))
        }
    },
    dismissButton = {
        TextButton(
            onClick = onClose
        ) {
            Text(text = stringResource(id = R.string.dialog_cancel))
        }
    }
)

@Composable
fun FailureDialog(
    name: String,
    message: String,
    onClose: () -> Unit,
) = AlertDialog(
    shape = RoundedCornerShape(20.dp),
    onDismissRequest = onClose,
    title = { Text(text = name) },
    text = {
        Text(
            text = message,
            modifier = Modifier
                .requiredHeightIn(max = 280.dp)
                .verticalScroll(rememberScrollState())
        )
    },
    confirmButton = {
        TextButton(
            onClick = onClose
        ) {
            Text(text = stringResource(id = R.string.dialog_ok))
        }
    }
)