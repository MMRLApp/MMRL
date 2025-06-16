package com.dergoogler.mmrl.ui.screens.modules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.local.State
import com.dergoogler.mmrl.model.online.Blacklist
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.ui.component.VersionItemBottomSheet
import com.dergoogler.mmrl.ui.component.scrollbar.VerticalFastScrollbar
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.ModulesViewModel
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasAction
import com.dergoogler.mmrl.ui.activity.terminal.action.ActionActivity
import com.dergoogler.mmrl.ui.component.DropdownMenu
import com.dergoogler.mmrl.ui.component.button.FilledTonalDoubleButton
import com.dergoogler.mmrl.ui.component.scaffold.ScaffoldScope
import com.dergoogler.mmrl.webui.model.WebUIConfig
import com.dergoogler.mmrl.webui.model.WebUIConfig.Companion.webUiConfig
import dev.dergoogler.mmrl.compat.core.LocalUriHandler

data class ModuleCacheData(
    val displayName: String,
    val versionAuthor: String,
    val updateTime: String?,
    val formattedSize: String,
    val isWebUIPackageInstalled: Boolean
)

@Composable
fun ScaffoldScope.ModulesList(
    list: List<LocalModule>,
    state: LazyListState,
    onDownload: (LocalModule, VersionItem, Boolean) -> Unit,
    viewModel: ModulesViewModel,
    cachedModuleData: Map<String, ModuleCacheData> = emptyMap(),
) = Box(
    modifier = Modifier.fillMaxSize()
) {
    this@ModulesList.ResponsiveContent {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = list,
                key = { it.id }
            ) { module ->
                ModuleItem(
                    module = module,
                    viewModel = viewModel,
                    onDownload = onDownload,
                    cachedData = cachedModuleData[module.id.toString()]
                )
            }
        }
    }

    VerticalFastScrollbar(
        state = state,
        modifier = Modifier.align(Alignment.CenterEnd)
    )
}

@Composable
fun ModuleItem(
    module: LocalModule,
    onDownload: (LocalModule, VersionItem, Boolean) -> Unit,
    viewModel: ModulesViewModel,
    cachedData: ModuleCacheData? = null,
) {
    val userPreferences = LocalUserPreferences.current

    val ops by remember(userPreferences.useShellForModuleStateChange, module.state) {
        derivedStateOf {
            viewModel.createModuleOps(
                userPreferences.useShellForModuleStateChange,
                module
            )
        }
    }

    val blacklist = viewModel.getBlacklist(module.id.toString())
    val isBlacklisted = Blacklist.isBlacklisted(blacklist)

    val context = LocalContext.current

    val item = viewModel.getVersionItem(module)
    val progress = viewModel.getProgress(item)

    val isProviderAlive = viewModel.isProviderAlive

    var open by remember { mutableStateOf(false) }
    if (open && item != null) {
        VersionItemBottomSheet(
            isUpdate = true,
            item = item,
            isProviderAlive = isProviderAlive,
            onDownload = { onDownload(module, item, it) },
            onClose = { open = false },
            isBlacklisted = isBlacklisted
        )
    }

    ModuleItem(
        isProviderAlive = isProviderAlive,
        isBlacklisted = Blacklist.isBlacklisted(blacklist),
        module = module,
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
            val enabled = with(viewModel.platform) {
                when {
                    isKernelSuNext || isKernelSU || isAPatch -> isProviderAlive && module.state != State.UPDATE
                    else -> isProviderAlive
                }
            }

            Switch(
                checked = module.state == State.ENABLE,
                onCheckedChange = ops.toggle,
                enabled = enabled && module.state != State.REMOVE
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
            module.hasAction.takeTrue {
                ActionButton(
                    enabled = isProviderAlive && module.state != State.REMOVE && module.state != State.DISABLE,
                    onClick = {
                        ActionActivity.start(
                            context = context,
                            modId = module.id
                        )
                    }
                )
            }
        },
        trailingButton = {
            if (item != null) {
                UpdateButton(
                    enabled = item.versionCode > module.versionCode,
                    onClick = { open = true }
                )

                Spacer(modifier = Modifier.width(12.dp))
            }

            RemoveOrRestore(
                module = module,
                enabled = isProviderAlive && (!(viewModel.moduleCompatibility.canRestoreModules && userPreferences.useShellForModuleStateChange) || module.state != State.REMOVE),
                onClick = ops.change
            )
        },
        cachedDisplayName = cachedData?.displayName,
        cachedVersionAuthor = cachedData?.versionAuthor,
        cachedUpdateTime = cachedData?.updateTime,
        cachedFormattedSize = cachedData?.formattedSize,
        isWebUIPackageInstalled = cachedData?.isWebUIPackageInstalled ?: false
    )
}

@Composable
private fun UpdateButton(
    enabled: Boolean,
    onClick: () -> Unit,
) = FilledTonalButton(
    onClick = onClick,
    enabled = enabled,
    contentPadding = PaddingValues(horizontal = 12.dp)
) {
    Icon(
        modifier = Modifier.size(20.dp),
        painter = painterResource(id = R.drawable.device_mobile_down),
        contentDescription = null
    )

    Spacer(modifier = Modifier.width(6.dp))
    Text(
        text = stringResource(id = R.string.module_update)
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RemoveOrRestore(
    module: LocalModule,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(
                id = if (module.state == State.REMOVE) {
                    R.drawable.rotate
                } else {
                    R.drawable.trash
                }
            ),
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(6.dp))
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

    /*
     val context = LocalContext.current
     val density = LocalDensity.current
     var offset = Offset.Zero
     val config = module.webUiConfig

     var expanded by remember { mutableStateOf(false) }
     Box {
         FilledTonalDoubleButton(
             onClick = onClick,
             onDropdownClick = {
                 expanded = !expanded
             },
             dropdownBtnModifier = Modifier
                 .pointerInteropFilter {
                     offset = Offset(it.x, it.y)
                     false
                 },
             enabled = enabled,
             dropdownIcon = R.drawable.dots_vertical,
             contentPadding = PaddingValues(horizontal = 12.dp)
         ) {
             Icon(
                 modifier = Modifier.size(20.dp),
                 painter = painterResource(
                     id = if (module.state == State.REMOVE) {
                         R.drawable.rotate
                     } else {
                         R.drawable.trash
                     }
                 ),
                 contentDescription = null
             )

             Spacer(modifier = Modifier.width(6.dp))
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

         val offsets = with(density) {
             DpOffset(
                 offset.x.toDp(),
                 offset.y.toDp()
             )
         }

         DropdownMenu(
             expanded = expanded,
             offset = offsets,
             onDismissRequest = { expanded = false }
         ) {
             when {
                 config.hasWebUIShortcut(context) -> RemoveShortcut(config)
                 module.state == State.REMOVE -> RemoveShortcut(config)
                 else -> CreateShortcut(config)
             }
         }
     }*/
}

@Composable
private fun CreateShortcut(config: WebUIConfig) {
    val context = LocalContext.current

//    DropdownMenuItem(
//        text = { Text(stringResource(R.string.create_webui_shortcut)) },
//        leadingIcon = {
//            Icon(
//                painter = painterResource(R.drawable.link),
//                contentDescription = null
//            )
//        },
//        enabled = config.canAddWebUIShortcut(),
//        onClick = {
//            config.createShortcut(context, WebUIActivity::class.java)
//        }
//    )
}

@Composable
private fun RemoveShortcut(config: WebUIConfig) {
    val context = LocalContext.current

    DropdownMenuItem(
        text = { Text(stringResource(R.string.remove_webui_shortcut)) },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.unlink),
                contentDescription = null
            )
        },
        onClick = {
            config.removeShortcut(context)
        }
    )
}

@Composable
private fun ActionButton(
    enabled: Boolean,
    onClick: () -> Unit,
) = FilledTonalButton(
    onClick = onClick,
    enabled = enabled,
    contentPadding = PaddingValues(horizontal = 12.dp)
) {
    Icon(
        modifier = Modifier.size(20.dp),
        painter = painterResource(id = R.drawable.player_play),
        contentDescription = null
    )
}