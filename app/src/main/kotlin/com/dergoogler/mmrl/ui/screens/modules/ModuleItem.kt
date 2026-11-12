package com.dergoogler.mmrl.ui.screens.modules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.model.local.versionDisplay
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.config
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toFormattedFileSize
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.dergoogler.mmrl.ui.component.LocalCover
import com.dergoogler.mmrl.ui.providable.LocalModule
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.toFormattedDateSafely
import dev.mmrlx.compose.layout.flashlightCard
import dev.mmrlx.compose.ui.Badge
import dev.mmrlx.compose.ui.BadgeVariant
import dev.mmrlx.compose.ui.HorizontalDivider
import dev.mmrlx.compose.ui.LinearProgressIndicator
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.scaffold.PositionLayout
import dev.mmrlx.compose.ui.theme.MMRLXTheme

@Composable
fun ModuleItem(
    progress: Float,
    indeterminate: Boolean = false,
    alpha: Float = 1f,
    decoration: TextDecoration = TextDecoration.None,
    switch: @Composable (() -> Unit?)? = null,
    indicator: @Composable (() -> Unit?)? = null,
    startTrailingButton: @Composable (RowScope.() -> Unit)? = null,
    trailingButton: @Composable (RowScope.() -> Unit),
) {
    val colors = MMRLXTheme.colors

    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.modulesMenu
    val module = LocalModule.current

    PositionLayout(
        modifier = Modifier
            .fillMaxWidth()
            .flashlightCard(),
    ) {
        indicator.nullable {
            Box(
                modifier = Modifier.absolute(Alignment.Center),
            ) {
                it()
            }
        }

        Column(
            modifier = Modifier.relative(),
        ) {
            module.config.cover.nullable(menu.showCover) { it ->
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
                                endY = 0f,
                            ),
                        ),
                        inputStream = it.newInputStream(),
                    )
                }
            }

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .alpha(alpha)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = module.name,
                        style = MMRLXTheme.typography.titleSmall,
                        color = colors.foreground,
                    )

                    Text(
                        text = stringResource(
                            id = R.string.module_version_author,
                            module.versionDisplay,
                            module.author,
                        ),
                        style = MMRLXTheme.typography.bodySmall,
                        textDecoration = decoration,
                        color = colors.mutedForeground,
                    )

                    if (module.lastUpdated != 0L && menu.showUpdatedTime) {
                        Text(
                            text = stringResource(
                                id = R.string.module_update_at,
                                module.lastUpdated.toFormattedDateSafely,
                            ),
                            style = MMRLXTheme.typography.bodySmall,
                            textDecoration = decoration,
                            color = colors.mutedForeground,
                        )
                    }
                }

                switch?.invoke()
            }

            Text(
                modifier = Modifier
                    .alpha(alpha)
                    .padding(horizontal = 16.dp),
                text = module.description,
                style = MMRLXTheme.typography.bodySmall,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                textDecoration = decoration,
                color = colors.mutedForeground,
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (userPreferences.developerMode) {
                    Badge(
                        text = module.id.id,
                    )
                }

                Badge(
                    text = module.size.toFormattedFileSize(),
                    variant = BadgeVariant.Secondary
                )
            }

            when {
                indeterminate -> {
                    LinearProgressIndicator(
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(2.dp)
                            .fillMaxWidth(),
                    )
                }

                progress != 0f -> {
                    LinearProgressIndicator(
                        progress = { progress },
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(1.5.dp)
                            .fillMaxWidth(),
                    )
                }

                else -> {
                    HorizontalDivider(
                        thickness = 1.5.dp,
                        color = colors.border,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                startTrailingButton?.invoke(this)

                Spacer(modifier = Modifier.weight(1f))

                trailingButton.invoke(this)
            }
        }
    }
}

@Composable
fun StateIndicator(
    @DrawableRes icon: Int,
    color: Color = MMRLXTheme.colors.mutedForeground,
) = Image(
    modifier = Modifier.requiredSize(150.dp),
    painter = painterResource(id = icon),
    contentDescription = null,
    alpha = 0.1f,
    colorFilter = ColorFilter.tint(color),
)
