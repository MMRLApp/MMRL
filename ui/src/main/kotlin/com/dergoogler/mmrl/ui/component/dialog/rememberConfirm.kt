package com.dergoogler.mmrl.ui.component.dialog

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.R

data class ConfirmData(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit,
    val onClose: (() -> Unit)? = null,
    val confirmText: String? = null,
    val closeText: String? = null,
)

/**
 * Displays a confirmation dialog using Jetpack Compose.
 *
 * This function is an extension function for `Context` and is intended to be used
 * within an Activity. It adds a `ComposeView` to the Activity's content view
 * to display the confirmation dialog.
 *
 * @param confirmData An object containing the data for the confirmation dialog,
 *                    including title, description, and callbacks for confirm/close actions.
 * @param colorScheme The MaterialTheme ColorScheme to be applied to the dialog.
 */
fun Context.confirm(
    confirmData: ConfirmData,
    colorScheme: ColorScheme,
) {
    (this as? Activity)?.addContentView(
        ComposeView(this).apply {
            setContent {
                var showDialog by remember { mutableStateOf(true) }

                if (showDialog) {
                    MaterialTheme(colorScheme = colorScheme) {
                        ConfirmDialog(
                            onDismissRequest = {
                                showDialog = false

                                if (confirmData.onClose != null) {
                                    confirmData.onClose()
                                    return@ConfirmDialog
                                }
                            },
                            closeText =
                                confirmData.closeText
                                    ?: stringResource(id = R.string.cancel),
                            confirmText =
                                confirmData.confirmText
                                    ?: stringResource(id = R.string.confirm),
                            title = confirmData.title,
                            description = confirmData.description,
                            onClose =
                                confirmData.onClose.nullable {
                                    {
                                        showDialog = false
                                        it()
                                    }
                                },
                            onConfirm = {
                                showDialog = false
                                confirmData.onConfirm()
                            },
                        )
                    }
                }
            }
        },
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ),
    )
}

@Composable
fun rememberConfirm(context: Context = LocalContext.current): (ConfirmData) -> Unit {
    val theme = MaterialTheme.colorScheme

    val confirm: (ConfirmData) -> Unit =
        remember {
            { confirm ->
                context.confirm(confirm, theme)
            }
        }

    return confirm
}
