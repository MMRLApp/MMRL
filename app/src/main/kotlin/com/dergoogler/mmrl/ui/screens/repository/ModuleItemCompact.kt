package com.dergoogler.mmrl.ui.screens.repository

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.dergoogler.mmrl.ui.component.Logo
import com.dergoogler.mmrl.ui.component.text.IconText
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalOnlineModuleState
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.toFormattedDateSafely

@Composable
fun ModuleItemCompact(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    showLabels: Boolean = true,
    showLastUpdated: Boolean = true,
    sourceProvider: String? = null,
) = Surface(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    enabled = enabled,
    shape = RoundedCornerShape(10.dp)
) {
    val module = LocalOnlineModule.current
    val state = LocalOnlineModuleState.current

    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.repositoryMenu
    val hasLabel =
        showLabels && ((state.hasLicense && menu.showLicense) || state.installed || (module.track.hasAntifeatures && menu.showAntiFeatures))
    val isVerified = module.isVerified && menu.showVerified

    Row(
        modifier = Modifier.padding(all = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (menu.showIcon) {
            if (module.icon != null) {
                AsyncImage(
                    model = module.icon,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentDescription = null
                )
            } else {
                Logo(
                    icon = R.drawable.box,
                    modifier = Modifier.size(40.dp),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            IconText(
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                tint = MaterialTheme.colorScheme.surfaceTint,
                alignment = Alignment.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                text = module.name,
                resId = isVerified nullable R.drawable.rosette_discount_check,
            )

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = module.author,
                style = MaterialTheme.typography.bodyMedium.copy(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = module.versionDisplay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            sourceProvider.nullable {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.surfaceTint
                )
            }

            menu.showUpdatedTime.nullable(showLastUpdated) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        id = R.string.module_update_at,
                        state.lastUpdated.toFormattedDateSafely
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (hasLabel) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (menu.showLicense && module.hasLicense) {
                        module.license?.let { LabelItem(text = it) }
                    }

                    if (menu.showAntiFeatures) {
                        module.track.antifeatures?.let {
                            if (it.isNotEmpty()) {
                                LabelItem(
                                    text = stringResource(id = R.string.view_module_antifeatures),
                                    style = LabelItemDefaults.style.copy(
                                        containerColor = MaterialTheme.colorScheme.onTertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                )
                            }
                        }
                    }

                    when {
                        state.updatable ->
                            LabelItem(
                                text = stringResource(id = R.string.module_new),
                                style = LabelItemDefaults.style.copy(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            )

                        state.installed ->
                            LabelItem(text = stringResource(id = R.string.module_installed))
                    }
                }
            }
        }
    }
}