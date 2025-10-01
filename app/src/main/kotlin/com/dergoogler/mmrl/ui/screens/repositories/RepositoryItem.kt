package com.dergoogler.mmrl.ui.screens.repositories

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.state.RepoState
import com.dergoogler.mmrl.ui.component.BottomSheet
import com.dergoogler.mmrl.ui.component.Cover
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import dev.dergoogler.mmrl.compat.core.LocalUriHandler
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.shareText
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Icon
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.utils.toFormattedDateSafely

@Composable
fun RepositoryItem(
    repo: RepoState,
    isDemoMode: Boolean = false,
    onClick: () -> Unit,
    update: () -> Unit,
    delete: () -> Unit,
) {
    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.repositoriesMenu
    val context = LocalContext.current
    val (alpha, textDecoration) = when {
        !repo.compatible -> 0.5f to TextDecoration.LineThrough
        else -> 1f to TextDecoration.None
    }

    val isEnabled = repo.compatible && !isDemoMode

    val repoCover = repo.cover.nullable(menu.showCover) { it }

    Card(
        enabled = repo.compatible,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.relative()
        ) {
            repoCover.nullable(menu.showCover) {
                if (it.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Cover(
                            modifier = Modifier.fadingEdge(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black,
                                    ),
                                    startY = Float.POSITIVE_INFINITY,
                                    endY = 0f
                                ),
                            ),
                            url = it,
                        )

                        menu.showModulesCount.takeTrue {
                            Box(
                                modifier = Modifier
                                    .absolutePadding(
                                        top = 16.dp,
                                        right = 16.dp
                                    )
                                    .align(Alignment.TopEnd),
                            ) {
                                ModuleCountLabelItem(repo)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(alpha),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = repo.name,
                        style = MaterialTheme.typography.titleSmall
                            .copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = textDecoration
                    )

                    menu.showUpdatedTime.takeTrue {
                        Text(
                            text = stringResource(
                                id = R.string.module_update_at,
                                repo.timestamp.toFormattedDateSafely
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textDecoration = textDecoration
                        )
                    }
                }

                if (repoCover == null && menu.showModulesCount) {
                    ModuleCountLabelItem(repo)
                }
            }

            repo.description.nullable {
                Text(
                    modifier = Modifier
                        .alpha(alpha = alpha)
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp),
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            HorizontalDivider(
                thickness = 1.5.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var open by remember { mutableStateOf(false) }
                if (open) {
                    BottomSheetForItem(
                        repo = repo,
                        onDelete = delete,
                        onClose = { open = false }
                    )
                }

                CardButtonItem(
                    icon = R.drawable.share,
                    enabled = isEnabled,
                    onClick = { context.shareText(repo.url) }
                )

                Spacer(Modifier.weight(1f))

                CardButtonItem(
                    icon = R.drawable.at,
                    label = R.string.repo_options,
                    onClick = { open = true },
                    enabled = isEnabled
                )

                CardButtonItem(
                    icon = R.drawable.cloud_download,
                    label = R.string.repo_options_update,
                    onClick = update,
                    enabled = isEnabled
                )
            }
        }
    }
}

@Composable
private fun BottomSheetForItem(
    repo: RepoState, onDelete: () -> Unit, onClose: () -> Unit,
) = BottomSheet(onDismissRequest = onClose) {
    val browser = LocalUriHandler.current

    Column(
        modifier = Modifier
            .padding(bottom = 18.dp)
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleSmall
                        .copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stringResource(
                        id = R.string.module_update_at,
                        repo.timestamp.toFormattedDateSafely
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            LabelItem(
                text = stringResource(id = R.string.repo_modules, repo.size),
                upperCase = false
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(15.dp),
            value = repo.url,
            onValueChange = {},
            readOnly = true,
            singleLine = true
        )
    }

    List(
        modifier = Modifier
            .padding(bottom = 18.dp),
    ) {
        repo.support.nullable {
            ButtonItem(
                onClick = { browser.openUri(it) }
            ) {
                this.Icon(painter = painterResource(id = R.drawable.brand_git))
                Title(R.string.repo_options_support)
            }
        }

        repo.donate.nullable {
            ButtonItem(
                onClick = { browser.openUri(it) }
            ) {
                this.Icon(painter = painterResource(id = R.drawable.heart_handshake))
                Title(R.string.repo_options_donate)
            }
        }

        repo.website.nullable {
            ButtonItem(
                onClick = { browser.openUri(it) }
            ) {
                this.Icon(painter = painterResource(id = R.drawable.world_www))
                Title(R.string.repo_options_website)
            }
        }

        repo.submission.nullable {
            ButtonItem(
                onClick = { browser.openUri(it) }
            ) {
                this.Icon(painter = painterResource(id = R.drawable.cloud_upload))
                Title(R.string.repo_options_submission)
            }
        }

        HorizontalDivider(
            thickness = Dp.Hairline
        )

        ButtonItem(
            onClick = onDelete
        ) {
            this.Icon(painter = painterResource(id = R.drawable.trash))
            Title(R.string.repo_options_delete)
        }
    }
}

@Composable
private fun CardButtonItem(
    @DrawableRes icon: Int,
    @StringRes label: Int? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
) = FilledTonalButton(
    onClick = onClick,
    enabled = enabled,
    contentPadding = PaddingValues(horizontal = 12.dp)
) {
    Icon(
        modifier = Modifier.size(20.dp),
        painter = painterResource(id = icon),
        contentDescription = null
    )

    label?.let {
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(id = label)
        )
    }
}

@Composable
private fun ModuleCountLabelItem(repo: RepoState) {
    if (repo.compatible) {
        LabelItem(
            text = stringResource(id = R.string.repo_modules, repo.size),
            upperCase = false
        )
    } else {
        LabelItem(
            text = stringResource(id = R.string.repo_incompatible),
            style = LabelItemDefaults.style.copy(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        )
    }
}