package com.dergoogler.mmrl.ui.component.toolbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.R
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.icon.IconButton
import dev.mmrlx.compose.ui.theme.MMRLXTheme
import dev.mmrlx.compose.ui.toolbar.Toolbar
import dev.mmrlx.compose.ui.toolbar.ToolbarDefaults
import dev.mmrlx.compose.ui.toolbar.ToolbarScrollBehavior

@Composable
fun BlurSearchToolbar(
    modifier: Modifier = Modifier,
    isSearch: Boolean,
    autoFocus: Boolean = true,
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: (() -> Unit)? = null,
    title: @Composable () -> Unit,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = ToolbarDefaults.windowInsets,
    scrollBehavior: ToolbarScrollBehavior? = null,
) = Toolbar(
    modifier = modifier,
    actions = actions,
    windowInsets = windowInsets,
    scrollBehavior = scrollBehavior,
    navigationIcon =
        if (onClose != null && isSearch) {
            {
                IconButton(
                    onClick = onClose,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = null,
                    )
                }
            }
        } else {
            navigationIcon
        },
    title =
        if (isSearch) {
            {
                val focusRequester = remember { FocusRequester() }
                val keyboardController = LocalSoftwareKeyboardController.current

                LaunchedEffect(focusRequester) {
                    if (autoFocus) {
                        focusRequester.requestFocus()
                    }
                    keyboardController?.show()
                }

                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = query,
                    onValueChange = onQueryChange,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search,
                        ),
                    keyboardActions =
                        KeyboardActions {
                            defaultKeyboardAction(ImeAction.Search)
                        },
                    shape = RoundedCornerShape(15.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        ),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = null,
                        )
                    },
                    placeholder = {
                        Text(text = stringResource(id = R.string.search_placeholder))
                    },
                    singleLine = true,
                    textStyle = MMRLXTheme.typography.bodyLarge,
                )
            }
        } else {
            {
                title()
            }
        },
)
