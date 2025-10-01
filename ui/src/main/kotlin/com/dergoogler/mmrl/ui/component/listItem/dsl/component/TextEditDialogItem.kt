package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.R
import com.dergoogler.mmrl.ui.component.dialog.TextFieldDialog
import com.dergoogler.mmrl.ui.component.listItem.dsl.DialogItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.FromSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.ProvideTitleTypography
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Slot
import com.dergoogler.mmrl.ui.token.TypographyKeyTokens

data class TextEditDialogItemData(
    val value: String,
    val isError: Boolean,
)

@Composable
fun ListScope.TextEditDialogItem(
    enabled: Boolean = true,
    value: String,
    strict: Boolean = true,
    onValid: ((String) -> Boolean)? = null,
    onConfirm: (String) -> Unit,
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions? = null,
    content: @Composable (ListItemScope.(TextEditDialogItemData) -> Unit),
) {
    var open by remember { mutableStateOf(false) }

    var text by remember { mutableStateOf(value) }
    var isError by remember { mutableStateOf(false) }

    val onDone: () -> Unit = {
        onConfirm(text)
        open = false
    }

    onValid.nullable { c ->
        LaunchedEffect(c) {
            isError = !c(value)
        }
    }

    val kbActions = remember(isError, text) {
        keyboardActions ?: KeyboardActions {
            if (text.isNotBlank() && !isError) onDone()
        }
    }

    val kbOptions = remember {
        keyboardOptions ?: KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        )
    }

    val data = remember(text, isError) {
        TextEditDialogItemData(
            value = text,
            isError = isError
        )
    }

    ButtonItem(
        enabled = enabled,
        onClick = {
            open = true
        },
        content = {
            content(data)

            if (open) {
                TextFieldDialog(
                    onDismissRequest = {
                        open = false
                    },
                    title = {
                        ProvideTitleTypography(
                            token = TypographyKeyTokens.HeadlineSmall
                        ) {
                            FromSlot(ListItemSlot.Title) {
                                content(data)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = onDone,
                            enabled = !isError && (!strict || text.isNotEmpty())
                        ) {
                            Text(text = stringResource(id = R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                open = false
                            }
                        ) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    }
                ) { focusRequester ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Layout(
                            content = {
                                this@ButtonItem.content(data)

                                this@ButtonItem.Slot(DialogItemSlot.TextField) {
                                    OutlinedTextField(
                                        modifier = Modifier.focusRequester(focusRequester),
                                        textStyle = MaterialTheme.typography.bodyLarge,
                                        value = text,
                                        onValueChange = {
                                            text = it
                                            isError = onValid?.invoke(it) == false
                                        },
                                        singleLine = false,
                                        supportingText = {
                                            Layout(
                                                content = {
                                                    this@ButtonItem.content(data)
                                                }
                                            ) { measurables, constraints ->
                                                val supportingPlaceable =
                                                    measurables.firstOrNull { it.layoutId == DialogItemSlot.SupportingText }
                                                        ?.measure(constraints)

                                                val totalHeight = listOfNotNull(
                                                    supportingPlaceable?.height,
                                                ).sum()

                                                layout(constraints.maxWidth, totalHeight) {
                                                    var y = 0

                                                    supportingPlaceable?.let {
                                                        it.placeRelative(0, y)
                                                        y += it.height
                                                    }
                                                }
                                            }
                                        },
                                        isError = isError,
                                        keyboardOptions = kbOptions,
                                        keyboardActions = kbActions,
                                        shape = RoundedCornerShape(15.dp)
                                    )
                                }
                            }
                        ) { measurables, constraints ->
                            val spacing = 16.dp.roundToPx()

                            val descriptionPlaceable =
                                measurables.firstOrNull { it.layoutId == DialogItemSlot.Description }
                                    ?.measure(constraints)
                            val textFieldPlaceable =
                                measurables.first { it.layoutId == DialogItemSlot.TextField }
                                    .measure(constraints)

                            val totalHeight = listOfNotNull(
                                descriptionPlaceable?.height,
                                spacing.takeIf { descriptionPlaceable != null },
                                textFieldPlaceable.height
                            ).sum()

                            layout(constraints.maxWidth, totalHeight) {
                                var y = 0

                                descriptionPlaceable?.let {
                                    it.placeRelative(0, y)
                                    y += it.height + spacing
                                }

                                textFieldPlaceable.placeRelative(0, y)
                            }
                        }
                    }
                }
            }
        }
    )
}
