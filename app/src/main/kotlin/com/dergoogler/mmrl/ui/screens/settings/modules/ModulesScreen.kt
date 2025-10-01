package com.dergoogler.mmrl.ui.screens.settings.modules

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.WebUIEngine
import com.dergoogler.mmrl.ui.component.APatchLabel
import com.dergoogler.mmrl.ui.component.KernelSuLabel
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.RadioDialogItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Section
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.SwitchItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Labels
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun ModulesScreen2() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_modules,
    ) {
        Section(
            title = stringResource(id = R.string.settings_behavior),
        ) {
            SwitchItem(
                enabled = viewModel.isProviderAlive && viewModel.platform.isNotMagisk,
                checked = userPreferences.useShellForModuleStateChange && viewModel.platform.isNotMagisk,
                onChange = viewModel::setUseShellForModuleStateChange,
            ) {
                Title(R.string.settings_shell_module_state_change)
                Description(R.string.settings_shell_module_state_change_desc)
                Labels { KernelSuLabel(); APatchLabel() }
            }

            SwitchItem(
                enabled = viewModel.isProviderAlive && viewModel.platform.isNotMagisk,
                checked = userPreferences.useShellForModuleAction,
                onChange = viewModel::setUseShellForModuleAction,
            ) {
                Title(R.string.settings_use_generic_action)
                Description(R.string.settings_use_generic_action_desc)
                Labels { KernelSuLabel(); APatchLabel() }
            }
        }

        Section(
            title = stringResource(id = R.string.view_module_features_webui),
            divider = false
        ) {
            RadioDialogItem(
                enabled = viewModel.isProviderAlive,
                selection = userPreferences.webuiEngine,
                options = listOf(
                    RadioDialogItem(
                        value = WebUIEngine.WX,
                        title = stringResource(R.string.settings_webui_engine_wx)
                    ),
                    RadioDialogItem(
                        value = WebUIEngine.KSU,
                        title = stringResource(R.string.settings_webui_engine_ksu)
                    ),
                    RadioDialogItem(
                        value = WebUIEngine.PREFER_MODULE,
                        title = stringResource(R.string.settings_webui_engine_prefer_module)
                    )
                ),
                onConfirm = {
                    viewModel.setWebUIEngine(it.value)
                }
            ) {
                Title(R.string.settings_webui_engine)
                Description(R.string.settings_webui_engine_desc)
            }
        }
    }
}