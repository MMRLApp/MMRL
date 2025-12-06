package com.dergoogler.mmrl.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Event.Companion.isLoading
import com.dergoogler.mmrl.app.Event.Companion.isSucceeded
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.network.compose.requestString
import kotlinx.coroutines.launch

@Composable
fun VersionItemBottomSheet(
    state: SheetState = rememberModalBottomSheetState(),
    isUpdate: Boolean,
    item: VersionItem,
    isProviderAlive: Boolean,
    onDownload: (Boolean) -> Unit,
    onClose: () -> Unit,
    isBlacklisted: Boolean,
) {
    val hasChangelog by remember {
        derivedStateOf { item.changelog.isNotBlank() }
    }

    BottomSheet(
        onDismissRequest = onClose,
        sheetState = state,
        dragHandle = {
            if (hasChangelog) {
                BottomSheetDefaults.DragHandle()
            } else {
                Text(
                    modifier =
                        Modifier
                            .padding(all = 18.dp)
                            .fillMaxWidth(),
                    text = stringResource(id = R.string.view_module_version_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        when {
            hasChangelog -> {
                ButtonRow(
                    isUpdate = isUpdate,
                    enableInstall = isProviderAlive && !isBlacklisted,
                    state = state,
                    onDownload = onDownload,
                    onClose = onClose,
                )
                ChangelogItem(url = item.changelog)
            }

            else -> {
                ButtonColumn(
                    isUpdate = isUpdate,
                    enableInstall = isProviderAlive && !isBlacklisted,
                    state = state,
                    downloader = onDownload,
                    onClose = onClose,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ButtonRow(
    isUpdate: Boolean,
    enableInstall: Boolean,
    state: SheetState,
    onDownload: (Boolean) -> Unit,
    onClose: () -> Unit,
) = Row(
    modifier =
        Modifier
            .padding(horizontal = 18.dp)
            .padding(bottom = 18.dp)
            .align(Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
) {
    val scope = rememberCoroutineScope()

    OutlinedButton(
        enabled = enableInstall,
        onClick = {
            onDownload(true)
            scope.launch {
                onClose()
                state.hide()
            }
        },
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.device_mobile_down),
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

        Text(
            text =
                stringResource(
                    id =
                        if (isUpdate) {
                            R.string.module_update
                        } else {
                            R.string.module_install
                        },
                ),
        )
    }

    OutlinedButton(
        onClick = {
            onDownload(false)
            scope.launch {
                onClose()
                state.hide()
            }
        },
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.file_download),
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

        Text(text = stringResource(id = R.string.module_download))
    }
}

@Composable
private fun ChangelogItem(url: String) {
    var changelog by remember { mutableStateOf("") }
    val event =
        requestString(
            url = url,
            onSuccess = { changelog = it },
        )

    Box(
        modifier =
            Modifier
                .animateContentSize(spring(stiffness = Spring.StiffnessLow)),
    ) {
        AnimatedVisibility(
            visible = event.isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Loading(minHeight = 200.dp)
        }

        AnimatedVisibility(
            visible = event.isSucceeded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            MarkdownText(
                text = changelog,
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 18.dp),
            )
        }
    }
}

@Composable
private fun ColumnScope.ButtonColumn(
    isUpdate: Boolean,
    enableInstall: Boolean,
    state: SheetState,
    downloader: (Boolean) -> Unit,
    onClose: () -> Unit,
) = Column(
    modifier =
        Modifier
            .padding(bottom = 18.dp)
            .align(Alignment.CenterHorizontally),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    val scope = rememberCoroutineScope()

    ButtonItem(
        enabled = enableInstall,
        onClick = {
            downloader(true)
            scope.launch {
                onClose()
                state.hide()
            }
        },
        icon = R.drawable.device_mobile_down,
        text =
            stringResource(
                id =
                    if (isUpdate) {
                        R.string.module_update
                    } else {
                        R.string.module_install
                    },
            ),
    )

    ButtonItem(
        onClick = {
            downloader(false)
            scope.launch {
                onClose()
                state.hide()
            }
        },
        icon = R.drawable.file_download,
        text = stringResource(id = R.string.module_download),
    )
}

@Composable
private fun ButtonItem(
    onClick: () -> Unit,
    enabled: Boolean = true,
    @DrawableRes icon: Int,
    text: String,
) = Surface(
    onClick = onClick,
    enabled = enabled,
    modifier =
        Modifier
            .alpha(if (enabled) 1f else 0.5f)
            .fillMaxWidth(),
) {
    Row(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 18.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = icon),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
