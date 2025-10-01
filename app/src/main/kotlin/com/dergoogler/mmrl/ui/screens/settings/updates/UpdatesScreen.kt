package com.dergoogler.mmrl.ui.screens.settings.updates

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.service.ModuleService
import com.dergoogler.mmrl.service.ProviderService
import com.dergoogler.mmrl.service.RepositoryService
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.RadioDialogItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Section
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.SwitchItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun radioDialogItem(interval: Long): RadioDialogItem<Long> {
    return RadioDialogItem(
        title = pluralStringResource(id = R.plurals.hours, count = interval.toInt(), interval),
        value = interval
    )
}

@Destination<RootGraph>
@Composable
fun UpdatesScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current
    val snackbarHost = LocalSnackbarHost.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val optionsOfHours = listOf(
        radioDialogItem(1),
        radioDialogItem(2),
        radioDialogItem(3),
        radioDialogItem(4),
        radioDialogItem(5),
        radioDialogItem(6),
        radioDialogItem(10),
        radioDialogItem(12),
        radioDialogItem(16),
        radioDialogItem(24),
        radioDialogItem(48),
        radioDialogItem(72),
    )

    SettingsScaffold(
        title = R.string.settings_updates,
    ) {
        Section {
            ButtonItem(
                onClick = {
                    val intent =
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent, null)
                    } else {
                        Toast.makeText(
                            context,
                            "Cannot open notification settings",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Title(R.string.settings_open_notification_settings)
            }
        }

        Section(
            title = stringResource(id = R.string.settings_app)
        ) {

            SwitchItem(
                checked = userPreferences.checkAppUpdates,
                onChange = viewModel::setCheckAppUpdates
            ) {
                Title(R.string.settings_check_app_updates)
                Description(R.string.settings_check_app_updates_desc)
            }

            SwitchItem(
                checked = userPreferences.checkAppUpdatesPreReleases,
                enabled = userPreferences.checkAppUpdates,
                onChange = viewModel::setCheckAppUpdatesPreReleases
            ) {
                Title(R.string.settings_include_preleases)
            }
        }

        Section(
            title = stringResource(id = R.string.page_repository)
        ) {
            SwitchItem(
                checked = RepositoryService.isActive,
                onChange = {
                    scope.launch {
                        if (it) {
                            RepositoryService.start(
                                context,
                                userPreferences.autoUpdateReposInterval
                            )
                            snackbarHost.showSnackbar(context.getString(R.string.repository_service_started))
                        } else {
                            RepositoryService.stop(context)
                            while (RepositoryService.isActive) {
                                delay(100)
                            }
                            snackbarHost.showSnackbar(context.getString(R.string.repository_service_stopped))
                        }
                    }
                }
            ) {
                Title(R.string.settings_auto_update_repos)
                Description(R.string.settings_auto_update_repos_desc)
            }

            RadioDialogItem(
                selection = userPreferences.autoUpdateReposInterval,
                options = optionsOfHours,
                onConfirm = {
                    viewModel.setAutoUpdateReposInterval(it.value)
                }
            ) {
                Title(R.string.settings_repo_update_interval)
                Description(
                    R.string.settings_repo_update_interval_desc,
                    userPreferences.autoUpdateReposInterval
                )
            }
        }

        Section(
            title = stringResource(id = R.string.page_modules)
        ) {

            SwitchItem(
                checked = ModuleService.isActive,
                enabled = viewModel.isProviderAlive && ProviderService.isActive,
                onChange = {
                    scope.launch {
                        if (it) {
                            ModuleService.start(
                                context,
                                userPreferences.autoUpdateReposInterval
                            )
                            snackbarHost.showSnackbar(context.getString(R.string.module_service_started))
                        } else {
                            ModuleService.stop(context)
                            while (ModuleService.isActive) {
                                delay(100)
                            }
                            snackbarHost.showSnackbar(context.getString(R.string.module_service_stopped))
                        }
                    }
                }
            ) {
                Title(R.string.settings_check_modules_update)
                Description(R.string.settings_check_modules_update_desc)
            }

            RadioDialogItem(
                selection = userPreferences.checkModuleUpdatesInterval,
                options = optionsOfHours,
                enabled = viewModel.isProviderAlive && ModuleService.isActive && ProviderService.isActive,
                onConfirm = {
                    viewModel.setCheckModuleUpdatesInterval(it.value)
                }
            ) {
                Title(R.string.settings_check_modules_update_interval)
                Description(
                    R.string.settings_check_modules_update_interval_desc,
                    userPreferences.checkModuleUpdatesInterval
                )
            }
        }
    }
}