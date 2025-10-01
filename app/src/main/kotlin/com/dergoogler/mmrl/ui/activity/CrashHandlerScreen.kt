package com.dergoogler.mmrl.ui.activity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.shareText
import com.dergoogler.mmrl.ui.component.BottomSheet
import com.dergoogler.mmrl.ui.component.MarkdownText
import com.dergoogler.mmrl.ui.component.NavigationBarsSpacer
import com.dergoogler.mmrl.ui.component.TopAppBar
import com.dergoogler.mmrl.ui.component.text.TextWithIcon
import com.dergoogler.mmrl.ui.component.text.TextWithIconDefaults
import com.dergoogler.mmrl.ui.component.toolbar.ToolbarTitle

@Composable
fun CrashHandlerScreen(
    message: String, stacktrace: String, helpMessage: String?,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current

    val browser = LocalUriHandler.current

    val hasHelp = helpMessage != null
    var helperSheet by remember { mutableStateOf(false) }
    if (helperSheet && hasHelp) HelpBottomSheet(
        text = helpMessage!!,
        onClose = {
            helperSheet = false
        }
    )

    val borderModifier = 2.5.dp
    val borderRadius = 20.dp

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shape = helpMessage.nullable(default = RoundedCornerShape(20.dp)) {
                        RoundedCornerShape(
                            topStart = borderRadius,
                            topEnd = borderRadius,
                            bottomStart = borderModifier,
                            bottomEnd = borderModifier,
                        )
                    },
                ) {
                    SelectionContainer {
                        Text(
                            modifier = Modifier
                                .padding(16.dp)
                                .horizontalScroll(rememberScrollState()),
                            text = message,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                helpMessage.nullable {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        contentColor = MaterialTheme.colorScheme.surfaceTint,
                        shape = RoundedCornerShape(
                            topStart = borderModifier,
                            topEnd = borderModifier,
                            bottomStart = borderRadius,
                            bottomEnd = borderRadius,
                        ),
                        onClick = { helperSheet = true }
                    ) {
                        TextWithIcon(
                            style = TextWithIconDefaults.style.copy(
                                rightIcon = true,
                                textStyle = MaterialTheme.typography.labelLarge
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(
                                vertical = 8.dp,
                                horizontal = 24.dp
                            ),
                            icon = R.drawable.help_outlined,
                            text = stringResource(R.string.help)
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    SelectionContainer {
                        Text(
                            modifier = Modifier
                                .padding(16.dp)
                                .horizontalScroll(rememberScrollState()),
                            text = stacktrace,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }

            item {


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            browser.openUri("https://github.com/DerGoogler/MMRL/issues")
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.report_to_issues)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            context.shareText("$message\n\n$stacktrace")
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.copy_logs)
                        )
                    }
                }

                NavigationBarsSpacer()
            }
        }
    }
}

@Composable
private fun HelpBottomSheet(
    text: String,
    onClose: () -> Unit,
) = BottomSheet(onDismissRequest = onClose) {
    Column(modifier = Modifier.padding(16.dp)) {
        MarkdownText(
            text = text,
        )
    }
}

@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
) = TopAppBar(
    title = {
        ToolbarTitle(titleResId = R.string.we_hit_a_brick_crash)
    },
    scrollBehavior = scrollBehavior,
)