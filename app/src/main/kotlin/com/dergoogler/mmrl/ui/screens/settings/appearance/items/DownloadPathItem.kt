package com.dergoogler.mmrl.ui.screens.settings.appearance.items

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.ui.component.dialog.TextFieldDialog
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import java.io.File

@Composable
fun ListScope.DownloadPathItem(
    downloadPath: String,
    onChange: (String) -> Unit,
) {
    var edit by remember { mutableStateOf(false) }
    if (edit) OpenDocumentTreeDialog(
        path = downloadPath,
        onClose = { edit = false },
        onConfirm = { if (it != downloadPath) onChange(it) }
    )

    ButtonItem(
        onClick = { edit = true }
    ) {
        Title(R.string.settings_download_path)
        Description(File(downloadPath).absolutePath)
    }
}

@Composable
private fun OpenDocumentTreeDialog(
    path: String,
    onClose: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember {
        mutableStateOf(File(path).toRelativeString(Const.PUBLIC_DOWNLOADS))
    }

    TextFieldDialog(
        onDismissRequest = onClose,
        title = { Text(text = stringResource(id = R.string.settings_download_path)) },
        confirmButton = {
            TextButton(
                onClick = {
                    val new = Const.PUBLIC_DOWNLOADS.resolve(name)
                    onConfirm(new.path)
                    onClose()
                },
            ) {
                Text(text = stringResource(id = R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onClose
            ) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
        },
        launchKeyboard = false
    ) {
        OutlinedTextField(
            textStyle = MaterialTheme.typography.bodyLarge,
            value = name,
            onValueChange = { name = it },
            shape = RoundedCornerShape(15.dp),
            label = { Text(text = Const.PUBLIC_DOWNLOADS.absolutePath) },
            singleLine = true
        )
    }
}