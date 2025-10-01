package com.dergoogler.mmrl.ui.screens.moduleView.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.ui.component.BulletList
import com.dergoogler.mmrl.ui.component.HtmlText
import com.dergoogler.mmrl.ext.ifNotEmpty
import com.dergoogler.mmrl.ext.isNotNullOrEmpty
import com.dergoogler.mmrl.ui.component.text.BBCodeText

@Composable
fun InstallConfirmDialog(
    name: String,
    requires: List<OnlineModule>,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
    onConfirmDeps: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(id = R.string.view_module_install_confirm_title))
        },
        text = {
            Column {
                if (requires.isNotNullOrEmpty()) {
                    BBCodeText(
                        text = stringResource(
                            R.string.view_module_install_confirm_desc_deps,
                            name
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BulletList(
                        items = requires.map { it.name },
                    )
                } else {
                    BBCodeText(
                        text = stringResource(
                            R.string.view_module_install_confirm_desc,
                            name
                        )
                    )
                }
            }
        },
        onDismissRequest = {
            onClose()
        },
        confirmButton = {
            requires.ifNotEmpty {
                TextButton(
                    onClick = {
                        onConfirmDeps()
                    }
                ) {
                    Text(text = stringResource(id = R.string.view_module_install_confirm_confirm_deps))
                }
            }
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(text = stringResource(id = R.string.view_module_install_confirm_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onClose()
                }
            ) {
                Text(text = stringResource(id = R.string.install_screen_reboot_dismiss))
            }
        }
    )
}