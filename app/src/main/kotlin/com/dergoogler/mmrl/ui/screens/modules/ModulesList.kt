package com.dergoogler.mmrl.ui.screens.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.rememberTrue
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.local.State
import com.dergoogler.mmrl.model.online.Blacklist
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasAction
import com.dergoogler.mmrl.ui.activity.terminal.action.ActionActivity
import com.dergoogler.mmrl.ui.component.VersionItemBottomSheet
import com.dergoogler.mmrl.ui.component.scaffold.ScaffoldScope
import com.dergoogler.mmrl.ui.component.scrollbar.VerticalFastScrollbar
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalModule
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.ModulesViewModel
import dev.chrisbanes.haze.hazeSource

@Composable
fun ScaffoldScope.ModulesList(
    innerPadding: PaddingValues,
    list: List<LocalModule>,
    state: LazyListState,
    onDownload: (LocalModule, VersionItem, Boolean) -> Unit,
    viewModel: ModulesViewModel,
    isProviderAlive: Boolean,
) = Box(
    modifier = Modifier.fillMaxSize()
) {
    val paddingValues = LocalMainScreenInnerPaddings.current
    val layoutDirection = LocalLayoutDirection.current

    this@ModulesList.ResponsiveContent {
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = LocalHazeState.current),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = list,
                key = { it.id.id },
                contentType = { "module_item" }
            ) { module ->
                CompositionLocalProvider(
                    LocalModule provides module
                ) {
                    ModuleItem(
                        viewModel = viewModel,
                        onDownload = onDownload,
                        isProviderAlive = isProviderAlive,
                    )
                }
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
private fun ModuleItem(
    onDownload: (LocalModule, VersionItem, Boolean) -> Unit,
    viewModel: ModulesViewModel,
    isProviderAlive: Boolean,
) {
    val context = LocalContext.current
    val module = LocalModule.current
    val userPreferences = LocalUserPreferences.current

    val ops by remember(userPreferences.useShellForModuleStateChange, module.state) {
        derivedStateOf {
            viewModel.createModuleOps(
                userPreferences.useShellForModuleStateChange,
                module
            )
        }
    }

    val blacklist by remember(module.id) {
        derivedStateOf {
            viewModel.getBlacklist(module.id.toString())
        }
    }

    val isModuleSwitchChecked by remember(module.state) {
        derivedStateOf { module.state == State.ENABLE }
    }

    val isModuleEnabled by remember(module.state, isProviderAlive, viewModel.platform) {
        derivedStateOf {
            val enabled = with(viewModel.platform) {
                when {
                    isKernelSuNext || isKernelSU || isAPatch -> isProviderAlive && module.state != State.UPDATE
                    else -> isProviderAlive
                }
            }
            enabled && module.state != State.REMOVE
        }
    }

    val isActionEnabled by remember(isProviderAlive, module.state) {
        derivedStateOf {
            isProviderAlive && module.state != State.REMOVE && module.state != State.DISABLE
        }
    }

    val isBlacklisted by Blacklist.isBlacklisted(blacklist)

    val item = viewModel.getVersionItem(module)
    val progress = viewModel.getProgress(item)

    var open by remember { mutableStateOf(false) }

    VersionItemBottomSheetIfNeeded(
        open = open,
        item = item,
        isProviderAlive = isProviderAlive,
        onDownload = { onDownload(module, item!!, it) },
        onClose = { open = false },
        isBlacklisted = isBlacklisted
    )

    ModuleItem(
        isProviderAlive = isProviderAlive,
        isBlacklisted = isBlacklisted,
        progress = progress,
        indeterminate = ops.isOpsRunning,
        alpha = when (module.state) {
            State.DISABLE, State.REMOVE -> 0.5f
            else -> 1f
        },
        decoration = when (module.state) {
            State.REMOVE -> TextDecoration.LineThrough
            else -> TextDecoration.None
        },
        switch = {
            Switch(
                checked = isModuleSwitchChecked,
                onCheckedChange = ops.toggle,
                enabled = isModuleEnabled
            )
        },
        indicator = {
            when (module.state) {
                State.REMOVE -> StateIndicator(R.drawable.trash)
                State.UPDATE -> StateIndicator(R.drawable.device_mobile_down)
                else -> {}
            }
        },
        startTrailingButton = {
            module.hasAction.rememberTrue {
                ActionButton(
                    enabled = isActionEnabled,
                    onClick = remember {
                        {
                            ActionActivity.start(
                                context = context,
                                modId = module.id
                            )
                        }
                    }
                )
            }
        },
        trailingButton = {
            item?.let { itm ->
                val hasUpdate by remember(itm, module.versionCode) {
                    derivedStateOf { itm.versionCode > module.versionCode }
                }

                UpdateButton(
                    enabled = hasUpdate,
                    onClick = remember { { open = true } }
                )

                Spacer(modifier = Modifier.width(12.dp))
            }

            val isRemoveOrRestoreEnabled by remember(
                userPreferences.useShellForModuleStateChange,
                module.state,
                isProviderAlive
            ) {
                derivedStateOf {
                    isProviderAlive && (!(viewModel.moduleCompatibility.canRestoreModules && userPreferences.useShellForModuleStateChange) || module.state != State.REMOVE)
                }
            }

            RemoveOrRestore(
                module = module,
                enabled = isRemoveOrRestoreEnabled,
                onClick = ops.change
            )
        }
    )
}

@Composable
private fun VersionItemBottomSheetIfNeeded(
    open: Boolean,
    item: VersionItem?,
    isProviderAlive: Boolean,
    onDownload: (Boolean) -> Unit,
    onClose: () -> Unit,
    isBlacklisted: Boolean,
) {
    if (open && item != null) {
        VersionItemBottomSheet(
            isUpdate = true,
            item = item,
            isProviderAlive = isProviderAlive,
            onDownload = onDownload,
            onClose = onClose,
            isBlacklisted = isBlacklisted
        )
    }
}

@Composable
private fun UpdateButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentPadding = remember { PaddingValues(horizontal = 12.dp) }
    val iconSize = remember { Modifier.size(20.dp) }
    val spacerWidth = remember { Modifier.width(6.dp) }

    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = contentPadding
    ) {
        Icon(
            modifier = iconSize,
            painter = painterResource(id = R.drawable.device_mobile_down),
            contentDescription = null
        )

        Spacer(modifier = spacerWidth)
        Text(
            text = stringResource(id = R.string.module_update)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RemoveOrRestore(
    module: LocalModule,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentPadding = remember { PaddingValues(horizontal = 12.dp) }
    val iconSize = remember { Modifier.size(20.dp) }
    val spacerWidth = remember { Modifier.width(6.dp) }

    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = contentPadding
    ) {
        Icon(
            modifier = iconSize,
            painter = painterResource(
                id = if (module.state == State.REMOVE) {
                    R.drawable.rotate
                } else {
                    R.drawable.trash
                }
            ),
            contentDescription = null
        )

        Spacer(modifier = spacerWidth)
        Text(
            text = stringResource(
                id = if (module.state == State.REMOVE) {
                    R.string.module_restore
                } else {
                    R.string.module_remove
                }
            )
        )
    }
}

@Composable
private fun ActionButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentPadding = remember { PaddingValues(horizontal = 12.dp) }
    val iconSize = remember { Modifier.size(20.dp) }

    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = contentPadding
    ) {
        Icon(
            modifier = iconSize,
            painter = painterResource(id = R.drawable.player_play),
            contentDescription = null
        )
    }
}
