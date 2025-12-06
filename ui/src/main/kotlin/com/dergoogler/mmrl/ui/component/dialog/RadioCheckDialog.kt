package com.dergoogler.mmrl.ui.component.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.R

data class RadioOptionItem<I>(
    val value: I,
    val title: String? = null,
    val enabled: Boolean = true,
)

@Composable
fun <T> RadioCheckDialog(
    value: T,
    title: String,
    suffix: String? = null,
    prefix: String? = null,
    options: List<RadioOptionItem<T>>,
    onClose: () -> Unit,
    onConfirm: (RadioOptionItem<T>) -> Unit,
) {
    var selectedOption by remember { mutableStateOf(value) }

    val onDone: () -> Unit = {
        onConfirm(RadioOptionItem(selectedOption))
        onClose()
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    items = options,
                ) { option ->
                    val checked = option.value == selectedOption
                    val interactionSource = remember { MutableInteractionSource() }

                    if (option.title == null) return@items

                    Row(
                        modifier =
                            Modifier
                                .toggleable(
                                    enabled = option.enabled,
                                    value = checked,
                                    onValueChange = {
                                        selectedOption = option.value
                                    },
                                    role = Role.RadioButton,
                                    interactionSource = interactionSource,
                                    indication = ripple(),
                                ).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            enabled = option.enabled,
                            selected = checked,
                            onClick = null,
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        when {
                            prefix != null -> Text(text = "$prefix${option.title}")
                            suffix != null -> Text(text = "${option.title}$suffix")
                            else -> Text(text = option.title)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDone) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(id = R.string.cancel))
            }
        },
    )
}
