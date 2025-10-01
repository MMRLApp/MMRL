package com.dergoogler.mmrl.ui.component.dialog

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.R

data class PromptData(
    val title: String,
    val value: String,
    val onValid: ((String) -> Boolean)? = null,
    val onConfirm: (String) -> Unit,
    val onClose: () -> Unit,
    val dialog: DialogParameters.() -> Unit = {},
)

/**
 * Displays a prompt dialog with a text field.
 *
 * This function is an extension on [Context] and is intended to be called from an Activity.
 * It adds a [ComposeView] to the Activity's content view, which displays a Material Design
 * dialog containing an [OutlinedTextField].
 *
 * @param prompt The [PromptData] containing the configuration for the dialog,
 *               including title, initial value, validation logic, and callbacks.
 * @param colorScheme The [ColorScheme] to be used for theming the dialog.
 */
fun Context.prompt(promptData: PromptData, colorScheme: ColorScheme) {
    (this as? Activity)?.addContentView(
        ComposeView(this).apply {
            setContent {
                val dialogParameters = remember { DialogParameters() }.apply(promptData.dialog)
                var text by remember { mutableStateOf(promptData.value) }
                var isError by remember { mutableStateOf(false) }
                var showDialog by remember { mutableStateOf(true) }

                if (showDialog) {
                    MaterialTheme(colorScheme = colorScheme) {

                        val onDone = {
                            showDialog = false
                            promptData.onConfirm(text)
                        }

                        val onClose = {
                            showDialog = false
                            promptData.onClose()
                        }

                        promptData.onValid.nullable { c ->
                            LaunchedEffect(c) {
                                isError = c(promptData.value)
                            }
                        }

                        TextFieldDialog(
                            onDismissRequest = onClose,
                            title = { Text(text = promptData.title) },
                            confirmButton = {
                                TextButton(
                                    onClick = onDone,
                                    enabled = !isError && text.isNotBlank()
                                ) {
                                    Text(text = stringResource(id = R.string.confirm))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = onClose
                                ) {
                                    Text(text = stringResource(id = R.string.cancel))
                                }
                            }) { focusRequester ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                dialogParameters.desc.nullable {
                                    it(text)
                                }

                                OutlinedTextField(
                                    modifier = Modifier.focusRequester(focusRequester),
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    value = text,
                                    onValueChange = {
                                        promptData.onValid.nullable { c ->
                                            isError = c(it)
                                        }
                                        text = it
                                    },
                                    singleLine = false,
                                    supportingText = {
                                        dialogParameters.supportingText.nullable {
                                            it(isError)
                                        }
                                    },
                                    isError = isError,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions {
                                        if (text.isNotBlank()) onDone()
                                    },
                                    shape = RoundedCornerShape(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    )
}

@Composable
fun rememberPrompt(
    context: Context = LocalContext.current,
): (PromptData) -> Unit {
    val theme = MaterialTheme.colorScheme

    val confirm: (PromptData) -> Unit = remember {
        { prompt ->
            context.prompt(prompt, theme)
        }
    }

    return confirm
}
