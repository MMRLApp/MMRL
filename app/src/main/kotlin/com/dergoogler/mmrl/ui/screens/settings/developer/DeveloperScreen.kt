package com.dergoogler.mmrl.ui.screens.settings.developer

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.platform.model.ModId.Companion.toModId
import com.dergoogler.mmrl.ui.activity.terminal.action.ActionActivity
import com.dergoogler.mmrl.ui.activity.terminal.install.InstallActivity
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.mmrlx.compose.ui.list.ListItemScope
import dev.mmrlx.compose.ui.list.ListScope
import dev.mmrlx.compose.ui.list.component.ButtonItem
import dev.mmrlx.compose.ui.list.component.Item
import dev.mmrlx.compose.ui.list.component.Section
import dev.mmrlx.compose.ui.list.component.SwitchItem
import dev.mmrlx.compose.ui.list.component.item.Description
import dev.mmrlx.compose.ui.list.component.item.Title

@Composable
fun ListScope.DeveloperSwitch(
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
    checked: Boolean,
    content: @Composable ListItemScope.() -> Unit,
) {
    val userPrefs = LocalUserPreferences.current

    SwitchItem(
        checked = userPrefs.developerMode && checked,
        onChange = onChange,
        enabled = userPrefs.developerMode && enabled,
        content = content,
    )
}

@Destination<RootGraph>
@Composable
fun DeveloperScreen() {
    val context = LocalContext.current
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_developer,
    ) {
        Section {
            SwitchItem(
                checked = userPreferences.developerMode,
                onChange = viewModel::setDeveloperMode,
            ) {
                Title(R.string.settings_developer_mode)
                Description(R.string.settings_developer_mode_desc)
            }

            DeveloperSwitch(
                checked = userPreferences.devAlwaysShowUpdateAlert,
                onChange = viewModel::setDevAlwaysShowUpdateAlert,
            ) {
                Title(R.string.settings_always_show_update_alert)
            }
        }

        Section {
            ButtonItem(
                onClick = {
                    InstallActivity.start(
                        context = context,
                        uri = "FUQDUP".toUri(),
                        confirm = false,
                    )
                }
            ) {
                Title(R.string.settings_cause_module_install_fail)
                Description(R.string.settings_cause_module_install_fail_desc)
            }

            ButtonItem(
                onClick = {
                    ActionActivity.start(
                        context = context,
                        modId = "FUQDUP".toModId(),
                    )
                }
            ) {
                Title(R.string.settings_cause_module_action_fail)
                Description(R.string.settings_cause_module_action_fail_desc)
            }
        }

        Section(
            divider = false,
        ) {
            Item {
                Title(stringResource(R.string.latest_commit_id))
                Description(BuildConfig.LATEST_COMMIT_ID)
            }
            Item {
                Title(stringResource(R.string.build_tools_version))
                Description(BuildConfig.BUILD_TOOLS_VERSION)
            }
            Item {
                Title(stringResource(R.string.compile_sdk))
                Description(BuildConfig.COMPILE_SDK)
            }
        }
    }
}
