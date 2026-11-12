package com.dergoogler.mmrl.ui.screens.moduleView.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.ifNotEmpty
import com.dergoogler.mmrl.ext.isNotNullOrEmpty
import com.dergoogler.mmrl.model.local.BulkModule
import com.dergoogler.mmrl.ui.activity.terminal.install.InstallActivity
import com.dergoogler.mmrl.ui.component.BulletList
import com.dergoogler.mmrl.ui.component.text.BBCodeText
import com.dergoogler.mmrl.ui.providable.LocalBulkInstall
import com.dergoogler.mmrl.viewmodel.ModuleViewModel
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.button.Button
import dev.mmrlx.compose.ui.button.ButtonVariant
import dev.mmrlx.compose.ui.dialog.Content
import dev.mmrlx.compose.ui.dialog.Dialog
import dev.mmrlx.compose.ui.dialog.Footer
import dev.mmrlx.compose.ui.dialog.Title
import timber.log.Timber

@Composable
fun InstallConfirmDialog() {
    val bulkInstallViewModel = LocalBulkInstall.current
    val context = LocalContext.current
    val vm = ModuleViewModel.current
    val module = vm.module
    val lastVersionItem = vm.lastVersionItem
    val requires = vm.requires

    val download = rememberDownload()

    if (vm.installConfirm)
        Dialog(
            onDismissRequest = {
                vm.installConfirm = false
            }
        ) {
            Title {
                Text(text = stringResource(id = R.string.view_module_install_confirm_title))
            }

            Content {
                Column {
                    if (requires.isNotNullOrEmpty()) {
                        BBCodeText(
                            text =
                                stringResource(
                                    R.string.view_module_install_confirm_desc_deps,
                                    module.name,
                                ),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        BulletList(
                            items = requires.map { it.name },
                        )
                    } else {
                        BBCodeText(
                            text =
                                stringResource(
                                    R.string.view_module_install_confirm_desc,
                                    module.name,
                                ),
                        )
                    }
                }
            }

            Footer {
                Button(
                    onClick = {
                        vm.installConfirm = false
                    },

                    variant = ButtonVariant.Outline,
                ) {
                    Text(text = stringResource(id = R.string.install_screen_reboot_dismiss))
                }

                requires.ifNotEmpty {
                    Button(
                        onClick = {
                            lastVersionItem?.let { item ->
                                val bulkModules = mutableListOf<BulkModule>()
                                bulkModules.add(
                                    BulkModule(
                                        id = module.id,
                                        name = module.name,
                                        versionItem = item,
                                    ),
                                )
                                bulkModules.addAll(
                                    requires.map { r ->
                                        BulkModule(
                                            id = r.id,
                                            name = r.name,
                                            versionItem = r.versions.first(),
                                        )
                                    },
                                )

                                bulkModules.ifNotEmpty {
                                    bulkInstallViewModel.downloadMultiple(
                                        items = bulkModules,
                                        onAllSuccess = { uris ->
                                            vm.installConfirm = false
                                            InstallActivity.start(
                                                context = context,
                                                uri = uris,
                                                confirm = false,
                                            )
                                        },
                                        onFailure = { err ->
                                            vm.installConfirm = false
                                            Timber.e(err)
                                        },
                                    )
                                }
                            }
                        },

                        variant = ButtonVariant.Ghost,
                    ) {
                        Text(text = stringResource(id = R.string.view_module_install_confirm_confirm_deps))
                    }
                }

                Button(
                    onClick = {
                        vm.installConfirm = false
                        lastVersionItem?.let { download(it, true) }
                    },
                    variant = ButtonVariant.Default,
                ) {
                    Text(text = stringResource(id = R.string.view_module_install_confirm_confirm))
                }
            }
        }
}
