package com.dergoogler.mmrl.ui.screens.repository

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.addIfNotThere
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.isNotNullOrEmpty
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.component.Cover
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.component.text.IconText
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalOnlineModuleState
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.toFormattedDateSafely

enum class LabelType {
    LICENSE,
    INSTALLED,
    ANTIFEATURES,
    CATEGORY,
    UPDATABLE
}

@Composable
fun ModuleItemDetailed(
    alpha: Float = 1f,
    onClick: () -> Unit = {},
    decoration: TextDecoration = TextDecoration.None,
    enabled: Boolean = true,
) {
    val module = LocalOnlineModule.current
    val state = LocalOnlineModuleState.current

    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.repositoryMenu
    val isVerified = module.isVerified && menu.showVerified

    val showLicenseLabel = state.hasLicense && menu.showLicense
    val showInstalledLabel = state.installed
    val showAntifeaturesLabel = module.track.hasAntifeatures && menu.showAntiFeatures
    val showCategoryLabel = module.categories.isNotNullOrEmpty() && menu.showCategory

    val labelsToShow = remember { mutableListOf<LabelType>() }
    if (showLicenseLabel) labelsToShow.addIfNotThere(LabelType.LICENSE)
    if (showInstalledLabel) labelsToShow.addIfNotThere(LabelType.INSTALLED)
    if (showAntifeaturesLabel) labelsToShow.addIfNotThere(LabelType.ANTIFEATURES)
    if (showCategoryLabel) labelsToShow.addIfNotThere(LabelType.CATEGORY)
    if (state.updatable) labelsToShow.addIfNotThere(LabelType.UPDATABLE)

    val hasLabel = labelsToShow.isNotEmpty()

    Card(
        enabled = enabled,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.relative()
        ) {
            module.cover.nullable(menu.showCover) {
                if (it.isNotEmpty()) {
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
                }
            }

            Row(
                modifier = Modifier.padding(all = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .alpha(alpha = alpha)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconText(
                        style = MaterialTheme.typography.titleSmall.copy(
                            textDecoration = decoration
                        ),
                        tint = MaterialTheme.colorScheme.surfaceTint,
                        alignment = Alignment.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        text = module.name,
                        resId = isVerified nullable R.drawable.rosette_discount_check,
                    )

                    Text(
                        text = stringResource(
                            id = R.string.module_version_author,
                            module.versionDisplay,
                            module.author
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = decoration,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (menu.showUpdatedTime) {
                        Text(
                            text = stringResource(
                                id = R.string.module_update_at,
                                state.lastUpdated.toFormattedDateSafely
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = decoration,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Text(
                modifier = Modifier
                    .alpha(alpha = alpha)
                    .padding(end = 16.dp, bottom = 16.dp, start = 16.dp),
                text = module.description
                    ?: stringResource(R.string.view_module_no_description),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.apply {
                    if (module.description.isNullOrBlank()) {
                        copy(
                            fontStyle = FontStyle.Italic
                        )
                    }
                },
                textDecoration = decoration,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.weight(1f))

            if (hasLabel) {
                Row(
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 16.dp, start = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    labelsToShow.forEach { labelType ->
                        when (labelType) {
                            LabelType.CATEGORY -> module.categories?.firstOrNull()
                                .nullable { category ->
                                    LabelItem(
                                        icon = R.drawable.category,
                                        text = category,
                                        style = LabelItemDefaults.style.copy(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                }

                            LabelType.LICENSE -> module.license.nullable { license ->
                                LabelItem(
                                    icon = R.drawable.tag,
                                    text = license
                                )
                            }

                            LabelType.ANTIFEATURES -> LabelItem(
                                icon = R.drawable.alert_triangle,
                                text = stringResource(id = R.string.view_module_antifeatures),
                                style = LabelItemDefaults.style.copy(
                                    containerColor = MaterialTheme.colorScheme.onTertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )

                            LabelType.INSTALLED -> LabelItem(
                                text = stringResource(id = R.string.module_installed)
                            )

                            LabelType.UPDATABLE -> LabelItem(
                                text = stringResource(id = R.string.module_new),
                                style = LabelItemDefaults.style.copy(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
