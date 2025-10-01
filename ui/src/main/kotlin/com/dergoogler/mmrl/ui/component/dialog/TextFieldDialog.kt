package com.dergoogler.mmrl.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.component.dialog.dsl.DialogContainer
import com.dergoogler.mmrl.ui.component.dialog.dsl.DialogContainerDefaults
import com.dergoogler.mmrl.ui.component.dialog.dsl.DialogContainerStyle
import com.dergoogler.mmrl.ui.component.dialog.dsl.item.Buttons
import com.dergoogler.mmrl.ui.component.dialog.dsl.item.Content
import com.dergoogler.mmrl.ui.component.dialog.dsl.item.Title
import com.dergoogler.mmrl.ui.component.lite.row.LiteRowScopeInstance.weight

@Composable
fun TextFieldDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    style: DialogContainerStyle = DialogContainerDefaults.style,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    launchKeyboard: Boolean = true,
    content: @Composable (FocusRequester) -> Unit
) = DialogContainer(
    onDismissRequest = onDismissRequest,
    modifier = modifier
        .wrapContentHeight()
        .requiredWidth(TextFieldDefaults.MinWidth + 40.dp),
    properties = properties
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(focusRequester) {
        if (launchKeyboard) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    title.nullable {
        Title(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon.nullable {
                CompositionLocalProvider(LocalContentColor provides style.iconContentColor) {
                    it()
                }
            }

            it()
        }
    }

    Content(
        modifier = Modifier.weight(1f, fill = false)
    ) {
        content(focusRequester)
    }

    Buttons {
        dismissButton?.invoke()
        confirmButton()
    }
}