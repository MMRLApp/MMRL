package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ui.R
import com.dergoogler.mmrl.ui.component.dialog.dsl.DialogContainer
import com.dergoogler.mmrl.ui.component.dialog.dsl.item.Buttons
import com.dergoogler.mmrl.ui.component.dialog.dsl.item.Content
import com.dergoogler.mmrl.ui.component.dialog.dsl.item.Title
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.FromSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.ProvideTitleTypography
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Start
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.component.text.BBCodeText
import com.dergoogler.mmrl.ui.token.TypographyKeyTokens

data class CheckboxItem<T>(
    val value: T,
    val checked: Boolean = false,
    val title: String? = null,
    val desc: String? = null,
    val enabled: Boolean = true,
)

@Composable
fun <T> ListScope.CheckboxDialogItem(
    enabled: Boolean = true,
    multiple: Boolean = false,
    strict: Boolean = true,
    maxChoices: Int = Int.MAX_VALUE,
    options: List<CheckboxItem<T>>,
    onConfirm: (List<CheckboxItem<T>>) -> Unit,
    content: @Composable (ListItemScope.(List<CheckboxItem<T>>) -> Unit),
) {
    var open by remember { mutableStateOf(false) }

    val initialSelectedOptions = remember(options) {
        options.filter { it.checked }
    }

    var selectedOptions by remember {
        mutableStateOf(initialSelectedOptions)
    }

    ButtonItem(
        enabled = enabled,
        onClick = { open = true },
        content = {
            content(selectedOptions)

            if (open) {
                this@CheckboxDialogItem.AlertCheckboxDialog(
                    strict = strict,
                    title = {
                        ProvideTitleTypography(
                            token = TypographyKeyTokens.HeadlineSmall
                        ) {
                            this@ButtonItem.FromSlot(ListItemSlot.Title) {
                                content(selectedOptions)
                            }
                        }
                    },
                    multiple = multiple,
                    maxChoices = maxChoices,
                    initialSelections = selectedOptions.map { it.value },
                    options = options,
                    onClose = { open = false },
                    onConfirm = { confirmedOptions ->
                        selectedOptions = confirmedOptions
                        onConfirm(confirmedOptions)
                    }
                )
            }
        }
    )
}

@Composable
private fun <T> ListScope.AlertCheckboxDialog(
    title: @Composable () -> Unit,
    multiple: Boolean = false,
    strict: Boolean = true,
    maxChoices: Int = Int.MAX_VALUE,
    initialSelections: List<T> = emptyList(),
    options: List<CheckboxItem<T>>,
    onDismiss: (() -> Unit)? = null,
    onClose: () -> Unit,
    onConfirm: (List<CheckboxItem<T>>) -> Unit,
) {
    var selectedValues by remember {
        mutableStateOf(initialSelections.toSet())
    }

    val selectedOptions = options.filter { selectedValues.contains(it.value) }
    val hasReachedMaxChoices = selectedValues.size >= maxChoices

    val onDone: () -> Unit = {
        onConfirm(selectedOptions)
        onClose()
    }

    val canSelectMore = !hasReachedMaxChoices || !multiple

    DialogContainer(
        onDismissRequest = {
            if (onDismiss != null) {
                onDismiss()
                return@DialogContainer
            }

            onClose()
        },
    ) {
        Title {
            title()

            if (multiple && maxChoices != Int.MAX_VALUE) {
                BBCodeText(
                    text = "${selectedValues.size}/[color=primary]$maxChoices[/color]",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Content(
            contentPadding = PaddingValues(0.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 450.dp)
                    .fadingEdge(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.03f to Color.Red,
                            0.97f to Color.Red,
                            1f to Color.Transparent
                        )
                    ),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(
                    items = options,
                    key = { it.value.hashCode() }
                ) { option ->
                    val isChecked = selectedValues.contains(option.value)
                    val interactionSource = remember { MutableInteractionSource() }

                    if (option.title == null) return@items

                    val isOptionEnabled = option.enabled &&
                            (isChecked || canSelectMore || !multiple)

                    Row(
                        modifier = Modifier
                            .toggleable(
                                enabled = isOptionEnabled,
                                value = isChecked,
                                onValueChange = { checked ->
                                    if (multiple) {
                                        selectedValues = if (checked) {
                                            if (selectedValues.size < maxChoices) {
                                                selectedValues + option.value
                                            } else {
                                                selectedValues
                                            }
                                        } else {
                                            selectedValues - option.value
                                        }
                                    } else {
                                        selectedValues = if (checked) {
                                            setOf(option.value)
                                        } else {
                                            emptySet()
                                        }
                                    }
                                },
                                role = Role.Checkbox,
                                interactionSource = interactionSource,
                                indication = ripple()
                            )
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        this@AlertCheckboxDialog.Item(
                            contentPadding = PaddingValues(
                                vertical = 8.dp,
                                horizontal = 25.dp
                            )
                        ) {
                            Title(option.title)

                            option.desc?.let {
                                Description(it)
                            }

                            Start {
                                Checkbox(
                                    enabled = isOptionEnabled,
                                    checked = isChecked,
                                    onCheckedChange = null
                                )
                            }
                        }
                    }
                }
            }
        }

        Buttons {
            TextButton(onClick = onClose) {
                Text(stringResource(id = R.string.cancel))
            }

            TextButton(
                onClick = onDone,
                enabled = if (multiple && strict) selectedValues.isNotEmpty() else true
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        }
    }
}
