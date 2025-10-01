package com.dergoogler.mmrl.ui.screens.modules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.isPackageInstalled
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.rememberTrue
import com.dergoogler.mmrl.model.local.State
import com.dergoogler.mmrl.model.local.versionDisplay
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.config
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasModConf
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasWebUI
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toFormattedFileSize
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.dergoogler.mmrl.ui.component.BottomSheet
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.dergoogler.mmrl.ui.component.LocalCover
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.component.card.CardScope
import com.dergoogler.mmrl.ui.component.card.component.Absolute
import com.dergoogler.mmrl.ui.component.lite.column.LiteColumn
import com.dergoogler.mmrl.ui.component.lite.row.LiteRow
import com.dergoogler.mmrl.ui.component.lite.row.LiteRowScope
import com.dergoogler.mmrl.ui.component.lite.row.VerticalAlignment
import com.dergoogler.mmrl.ui.component.text.BBCodeTag
import com.dergoogler.mmrl.ui.component.text.BBCodeText
import com.dergoogler.mmrl.ui.providable.LocalModule
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.toFormattedDateSafely
import com.dergoogler.mmrl.utils.webUILauncher
import dev.dergoogler.mmrl.compat.core.LocalUriHandler
import kotlinx.coroutines.launch

@Composable
fun ModuleItem(
    progress: Float,
    indeterminate: Boolean = false,
    alpha: Float = 1f,
    decoration: TextDecoration = TextDecoration.None,
    switch: @Composable() (() -> Unit?)? = null,
    indicator: @Composable() (CardScope.() -> Unit?)? = null,
    startTrailingButton: @Composable() (LiteRowScope.() -> Unit)? = null,
    trailingButton: @Composable() (LiteRowScope.() -> Unit),
    isBlacklisted: Boolean = false,
    isProviderAlive: Boolean,
) {
    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.modulesMenu
    val context = LocalContext.current
    val density = LocalDensity.current

    val module = LocalModule.current

    var requiredAppBottomSheet by remember { mutableStateOf(false) }

    val canWenUIAccessed = remember(isProviderAlive, module) {
        isProviderAlive && (module.hasWebUI || module.hasModConf) && module.state != State.REMOVE
    }

    val isWebUIXNotInstalled = remember(context) {
        !context.isPackageInstalled(userPreferences.webuixPackageName)
    }

    val launch = userPreferences.webUILauncher(context, module)

    val clicker: (() -> Unit)? = remember(canWenUIAccessed) {
        canWenUIAccessed nullable jump@{
            if (isWebUIXNotInstalled) {
                requiredAppBottomSheet = true
                return@jump
            }

            launch()
        }
    }

    if (requiredAppBottomSheet) {
        BottomSheetForWXP {
            requiredAppBottomSheet = false
        }
    }

    Card(
        border = isBlacklisted nullable BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.errorContainer
        ),
        onClick = clicker
    ) {
        indicator.nullable {
            Absolute(
                alignment = Alignment.Center,
            ) {
                it()
            }
        }

        LiteColumn(
            modifier = Modifier.relative(),
        ) {
            module.config.cover.nullable(menu.showCover) {
                val file = SuFile(module.id.moduleDir, it)

                file.exists {
                    LocalCover(
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
                        inputStream = it.newInputStream(),
                    )
                }
            }

            LiteRow(
                modifier = Modifier.padding(all = 16.dp),
                verticalAlignment = VerticalAlignment.Center
            ) {
                LiteColumn(
                    modifier = Modifier
                        .alpha(alpha = alpha)
                        .weight(1f),
                    spaceBetweenItem = 2.dp,
                ) {
                    val name = remember {
                        module.config.name ?: module.name
                    }

                    val prefix = fun(): String? {
                        if (!canWenUIAccessed) return null
                        if (module.hasWebUI) return "[icon=webui] "
                        if (module.hasModConf) return "[image=modconf] "
                        return null
                    }

                    BBCodeText(
                        text = name,
                        bbEnabled = false,
                        disabledTags = BBCodeTag.disableAllExcept(BBCodeTag.ICON, BBCodeTag.IMAGE),
                        iconContent = (canWenUIAccessed && module.hasWebUI) nullable {
                            Icon(
                                painter = painterResource(id = R.drawable.sandbox),
                                contentDescription = null,
                                tint = LocalContentColor.current,
                            )
                        },
                        imageContent = (canWenUIAccessed && module.hasModConf) nullable {
                            Image(
                                painter = painterResource(id = com.dergoogler.mmrl.ui.R.drawable.jetpackcomposeicon),
                                contentDescription = null,
                            )
                        },
                        prefix = prefix(),
                        style = MaterialTheme.typography.titleSmall,
                    )

                    Text(
                        text = stringResource(
                            id = R.string.module_version_author,
                            module.versionDisplay, module.author
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = decoration,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (module.lastUpdated != 0L && menu.showUpdatedTime) {
                        Text(
                            text = stringResource(
                                id = R.string.module_update_at,
                                module.lastUpdated.toFormattedDateSafely
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = decoration,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                switch?.invoke()
            }

            val bbEnabled = remember(module) {
                module.config.description != null
            }

            val desc = remember(module) { module.config.description ?: module.description }

            BBCodeText(
                modifier = Modifier
                    .alpha(alpha = alpha)
                    .padding(horizontal = 16.dp),
                text = desc,
                bbEnabled = bbEnabled,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                textDecoration = decoration,
                color = MaterialTheme.colorScheme.outline
            )

            LiteRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = VerticalAlignment.Center,
                spaceBetweenItem = 8.dp,
            ) {
                userPreferences.developerMode.rememberTrue {
                    LabelItem(
                        text = module.id.id,
                        upperCase = false
                    )
                }

                LabelItem(
                    text = module.size.toFormattedFileSize(),
                    style = LabelItemDefaults.style.copy(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }

            when {
                indeterminate -> LinearProgressIndicator(
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(2.dp)
                        .fillMaxWidth()
                )

                progress != 0f -> LinearProgressIndicator(
                    progress = { progress },
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(1.5.dp)
                        .fillMaxWidth()
                )

                else -> HorizontalDivider(
                    thickness = 1.5.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            LiteRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = VerticalAlignment.Center,
            ) {
                startTrailingButton?.invoke(this)
                Spacer(modifier = Modifier.weight(1f))
                trailingButton.invoke(this)
            }
        }
    }
}

@Composable
fun BottomSheetForWXP(
    onCancel: () -> Unit,
) {
    val browser = LocalUriHandler.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    BottomSheet(
        onDismissRequest = onCancel,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Icon(
                painter = painterResource(id = R.drawable.sandbox),
                contentDescription = null,
                modifier = Modifier.size(98.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.external_app_required_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.external_app_required_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            browser.openUri(
                                if (BuildConfig.IS_GOOGLE_PLAY_BUILD) {
                                    "https://play.google.com/store/apps/details?id=com.dergoogler.mmrl.wx"
                                } else {
                                    "https://github.com/MMRLApp/WebUI-X-Portable"
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.module_download))
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onCancel()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(com.dergoogler.mmrl.ui.R.string.cancel))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StateIndicator(
    @DrawableRes icon: Int,
    color: Color = MaterialTheme.colorScheme.outline,
) = Image(
    modifier = Modifier.requiredSize(150.dp),
    painter = painterResource(id = icon),
    contentDescription = null,
    alpha = 0.1f,
    colorFilter = ColorFilter.tint(color)
)
