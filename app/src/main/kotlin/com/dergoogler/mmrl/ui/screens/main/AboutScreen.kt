package com.dergoogler.mmrl.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.Logo
import com.dergoogler.mmrl.ui.component.MarkdownText
import com.dergoogler.mmrl.ui.component.card.OutlinedCard
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurNavigateUpToolbar
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.dergoogler.mmrl.compat.core.LocalUriHandler

@Destination<RootGraph>
@Composable
fun AboutScreen() = LocalScreenProvider {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val browser = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BlurNavigateUpToolbar(
                title = stringResource(id = R.string.settings_about),
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        this@Scaffold.ResponsiveContent {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(all = 16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Logo(
                    icon = R.drawable.launcher_outline,
                    modifier = Modifier.size(65.dp),
                    contentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    fraction = 0.7f
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = stringResource(
                            id = R.string.about_app_version,
                            BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { browser.openUri(Const.GITHUB_URL) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.github),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(id = R.string.about_github))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        FilledTonalButton(
                            onClick = { browser.openUri(Const.TRANSLATE_URL) }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.weblate),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text(text = stringResource(id = R.string.about_weblate))
                        }

                        FilledTonalButton(
                            onClick = { browser.openUri(Const.TELEGRAM_URL) }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.telegram),
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text(text = stringResource(id = R.string.about_telegram))
                        }
                    }
                }

                val style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedCard {

                    Text(
                        text = stringResource(id = R.string.about_desc1),
                        style = style
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    MarkdownText(
                        text = stringResource(
                            id = R.string.about_desc2,
                            "@sanmer(Sanmer) & @googler(Der_Googler)"
                        ),
                        style = style,
                        onTagClick = {
                            when (it) {
                                "sanmer" -> browser.openUri(Const.SANMER_GITHUB_URL)
                                "googler" -> browser.openUri(Const.GOOGLER_GITHUB_URL)
                            }
                        }
                    )
                }

                val paddingValues = LocalMainScreenInnerPaddings.current
                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
            }
        }
    }
}