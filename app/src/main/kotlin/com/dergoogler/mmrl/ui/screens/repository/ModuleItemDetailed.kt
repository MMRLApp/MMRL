package com.dergoogler.mmrl.ui.screens.repository

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.isNotNullOrEmpty
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.ui.component.Cover
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalOnlineModuleState
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import dev.mmrlx.compose.layout.flashlightCard
import dev.mmrlx.compose.ui.Avatar
import dev.mmrlx.compose.ui.Badge
import dev.mmrlx.compose.ui.BadgeVariant
import dev.mmrlx.compose.ui.HorizontalDivider
import dev.mmrlx.compose.ui.LocalTextStyle
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.text.FormatText
import dev.mmrlx.compose.ui.theme.MMRLXTheme

enum class LabelType {
    LICENSE,
    INSTALLED,
    ANTIFEATURES,
    CATEGORY,
    UPDATABLE,
}

@Composable
fun ModuleItemDetailed(
    alpha: Float = 1f,
    onClick: () -> Unit = {},
    decoration: TextDecoration = TextDecoration.None,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val module = LocalOnlineModule.current
    val state = LocalOnlineModuleState.current

    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.repositoryMenu
    val isVerified = module.isVerified && menu.showVerified

    val showLicenseLabel = state.hasLicense && menu.showLicense
    val showInstalledLabel = state.installed
    val showAntifeaturesLabel = module.track.hasAntifeatures && menu.showAntiFeatures
    val showCategoryLabel = module.categories.isNotNullOrEmpty() && menu.showCategory

    val labelsToShow = buildList {
        if (showLicenseLabel) {
            add(LabelType.LICENSE)
        }

        if (showInstalledLabel) {
            add(LabelType.INSTALLED)
        }

        if (showAntifeaturesLabel) {
            add(LabelType.ANTIFEATURES)
        }

        if (showCategoryLabel) {
            add(LabelType.CATEGORY)
        }

        if (state.updatable) {
            add(LabelType.UPDATABLE)
        }
    }

    val hasLabel = labelsToShow.isNotEmpty()

    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                enabled = enabled
            )
            .fillMaxWidth()
            .flashlightCard()
    ) {
        Column {
            module.cover.nullable(menu.showCover) {
                if (it.isNotEmpty()) {
                    Cover(
                        modifier =
                            Modifier.fadingEdge(
                                brush =
                                    Brush.verticalGradient(
                                        colors =
                                            listOf(
                                                Color.Transparent,
                                                Color.Black,
                                            ),
                                        startY = Float.POSITIVE_INFINITY,
                                        endY = 0f,
                                    ),
                            ),
                        url = it,
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .padding(all = 16.dp)
                        .alpha(alpha = alpha),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (menu.showIcon) {
                        val painter = rememberAsyncImagePainter(
                            model =
                                ImageRequest
                                    .Builder(context)
                                    .data(module.icon)
                                    .memoryCacheKey(module.icon)
                                    .diskCacheKey(module.icon)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                        )

                        val isError =
                            remember(painter.state) { painter.state is AsyncImagePainter.State.Error }

                        Avatar(
                            initials = module.name.take(
                                2
                            ).uppercase(),
                            size = 36.dp,
                            painter = if (isError) null else painter,
                        )
                    }

                    Column {
                        FormatText(
                            text = buildString {
//                                if (module.metaModule) {
//                                    append("%y ")
//                                }
                                append(module.name)
                                if (isVerified) {
                                    append(" %y")
                                }
                            },
                            style = MMRLXTheme.typography.titleSmall
                        ) {
//                            if (module.metaModule) {
//                                composable {
//                                    Badge(
//                                        text = "M",
//                                        variant = BadgeVariant.Secondary,
//                                    )
//                                }
//                            }

                            if (isVerified) {
                                composable {
                                    Icon(
                                        modifier = Modifier.size(fontSize.dp),
                                        painter = painterResource(id = R.drawable.rosette_discount_check),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.surfaceTint
                                    )
                                }
                            }
                        }

                        Text(
                            text = stringResource(
                                id = R.string.module_version_author,
                                module.versionDisplay,
                                module.author,
                            ),
                            style = MMRLXTheme.typography.labelSmall,
                            color = MMRLXTheme.colors.mutedForeground
                        )
                    }
                }

                Text(
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    text = module.description
                        ?: stringResource(R.string.view_module_no_description),
                    style = MMRLXTheme.typography.bodySmall.apply {
                        if (module.description.isNullOrBlank()) {
                            copy(
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    }
                )

                val stats = remember(menu) {
                    buildString {
                        if (menu.showStars && module.stars != null) {
                            append("%y %s")
                        }
                        if ((menu.showStars && module.stars != null) && menu.showUpdatedTime) {
                            append(" %s ")
                        }
                        if (menu.showUpdatedTime) {
                            append("%y %s")
                        }
                    }
                }

                if (stats.isNotEmpty()) {
                    FormatText(
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                        text = stats,
                        style = MMRLXTheme.typography.labelSmall,
                        color = MMRLXTheme.colors.mutedForeground
                    ) {
                        if (menu.showStars && module.stars != null) {
                            composable {
                                Icon(
                                    modifier = Modifier.size(fontSize.dp),
                                    painter = painterResource(R.drawable.star),
                                    tint = MMRLXTheme.colors.mutedForeground
                                )
                            }
                            string(module.stars.toString())
                        }

                        if ((menu.showStars && module.stars != null) && menu.showUpdatedTime) {
                            string("•")
                        }

                        if (menu.showUpdatedTime) {
                            composable {
                                Icon(
                                    modifier = Modifier.size(fontSize.dp),
                                    painter = painterResource(R.drawable.git_branch),
                                    tint = MMRLXTheme.colors.mutedForeground
                                )
                            }
                            string(state.lastUpdated.toFormattedDateSafely(userPreferences.datePattern))
                        }
                    }
                }


                if (hasLabel) {
                    HorizontalDivider(Modifier.padding(top = 8.dp))

                    FlowRow(
                        modifier =
                            Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        labelsToShow.forEach { labelType ->
                            when (labelType) {
                                LabelType.CATEGORY -> {
                                    module.categories
                                        ?.firstOrNull()
                                        .nullable { category ->
                                            Badge(
                                                variant = BadgeVariant.Secondary
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(
                                                        4.dp
                                                    )
                                                ) {
                                                    Icon(
                                                        modifier = Modifier.sizeByFontSize(),
                                                        painter = painterResource(R.drawable.category)
                                                    )
                                                    Text(text = category)
                                                }
                                            }
                                        }
                                }

                                LabelType.LICENSE -> {
                                    module.license.nullable { license ->
                                        Badge(
                                            modifier = Modifier.widthIn(max = 100.dp),
                                            variant = BadgeVariant.Default
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(
                                                    4.dp
                                                )
                                            ) {
                                                Icon(
                                                    modifier = Modifier.sizeByFontSize(),
                                                    painter = painterResource(R.drawable.tag)
                                                )
                                                Text(
                                                    text = license,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                LabelType.ANTIFEATURES -> {
                                    Badge(
                                        variant = BadgeVariant.Warning
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(
                                                4.dp
                                            )
                                        ) {
                                            Icon(
                                                modifier = Modifier.sizeByFontSize(),
                                                painter = painterResource(R.drawable.alert_triangle)
                                            )
                                            Text(text = stringResource(id = R.string.view_module_antifeatures))
                                        }
                                    }
                                }

                                LabelType.INSTALLED -> {
                                    Badge(
                                        variant = BadgeVariant.Default
                                    ) {
                                        Text(text = stringResource(id = R.string.module_installed))
                                    }
                                }

                                LabelType.UPDATABLE -> {
                                    Badge(
                                        variant = BadgeVariant.Outline
                                    ) {
                                        Text(text = stringResource(id = R.string.module_new))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// TODO: export
@Composable
fun Modifier.sizeByFontSize() = this.size(LocalTextStyle.current.fontSize.value.dp)

// TODO: export
fun Painter.isEmptyOrZero(): Boolean {
    val size = this.intrinsicSize
    return size == Size.Unspecified || size.width == 0f || size.height == 0f
}