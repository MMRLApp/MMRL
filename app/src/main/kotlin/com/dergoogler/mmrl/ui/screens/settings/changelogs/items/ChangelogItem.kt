package com.dergoogler.mmrl.ui.screens.settings.changelogs.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.model.online.Changelog
import com.dergoogler.mmrl.ui.component.BottomSheet
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.MarkdownText
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Labels
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title

@Composable
fun ListScope.ChangelogItem(changelog: Changelog) {
    var open by remember { mutableStateOf(false) }
    if (open) {
        ChangelogBottomSheet(changelog = changelog, onClose = { open = false })
    }

    ButtonItem(
        onClick = { open = true },
    ) {
        Title(changelog.versionName)
        Description(changelog.versionCode.toString())
        changelog.preRelease.takeTrue {
            Labels {
                LabelItem(
                    text =
                        stringResource(
                            id = R.string.pre_release,
                        ),
                )
            }
        }
    }
}

@Composable
fun ChangelogBottomSheet(
    changelog: Changelog,
    onClose: () -> Unit,
) = BottomSheet(onDismissRequest = onClose) {
    val browser = LocalUriHandler.current

    val topBottomFade =
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.03f to Color.Red,
            0.97f to Color.Red,
            1f to Color.Transparent,
        )

    Column(
        modifier =
            Modifier
                .padding(bottom = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fadingEdge(topBottomFade)
                    .verticalScroll(rememberScrollState()),
        ) {
            MarkdownText(
                text = "# ${changelog.versionName} (${changelog.versionCode})\n${changelog.changes}",
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier
                        .padding(horizontal = 18.dp)
                        .padding(top = 8.dp)
                        .padding(bottom = 18.dp),
            )
        }

        Button(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            onClick = {
                if (BuildConfig.IS_GOOGLE_PLAY_BUILD) {
                    browser.openUri(Const.GOOGLE_PLAY_DOWNLOAD)
                } else {
                    browser.openUri(Const.GITHUB_DOWNLOAD + "/tag/${changelog.versionName}")
                }
            },
        ) {
            Text(stringResource(id = R.string.module_download))
        }
    }
}
