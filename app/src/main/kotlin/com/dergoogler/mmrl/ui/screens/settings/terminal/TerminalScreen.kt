package com.dergoogler.mmrl.ui.screens.settings.terminal

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.WorkingMode.Companion.isRoot
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Section
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.SwitchItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun TerminalScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_terminal,
    ) {
        Section(
            title = stringResource(id = R.string.settings_appearance),
        ) {
            SwitchItem(
                checked = userPreferences.terminalTextWrap,
                onChange = viewModel::setTerminalTextWrap
            ) {
                Title(R.string.settings_text_wrap)
                Description(R.string.settings_text_wrap_desc)

            }

            SwitchItem(
                checked = userPreferences.showTerminalLineNumbers,
                onChange = viewModel::setShowTerminalLineNumbers
            ) {
                Title(R.string.settings_terminal_line_numbers)
                Description(R.string.settings_terminal_line_numbers_desc)
            }
        }

        Section(
            title = stringResource(id = R.string.settings_behavior),
            divider = false
        ) {
            SwitchItem(
                checked = userPreferences.clearInstallTerminal,
                onChange = viewModel::setClearInstallTerminal,
            ) {
                Title(R.string.settings_clear_install_terminal)
                Description(R.string.settings_clear_install_terminal_desc)
            }

            SwitchItem(
                checked = userPreferences.deleteZipFile,
                onChange = viewModel::setDeleteZipFile,
                enabled = userPreferences.workingMode.isRoot
            ) {
                Title(R.string.settings_delete_zip)
                Description(R.string.settings_delete_zip_desc)
            }


            SwitchItem(
                checked = userPreferences.allowCancelInstall,
                onChange = viewModel::setAllowCancelInstall,
            ) {
                Title(R.string.allow_cancel_installation)
            }

            SwitchItem(
                checked = userPreferences.allowCancelAction,
                onChange = viewModel::setAllowCancelAction,
            ) {
                Title(R.string.allow_cancel_action)
            }
        }
    }
}