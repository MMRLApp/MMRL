package com.dergoogler.mmrl.ui.screens.settings.developer

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Item
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Section
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.SwitchItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

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
        content = content
    )
}

@Destination<RootGraph>
@Composable
fun DeveloperScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_developer
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

        Section(
            divider = false
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
