package com.dergoogler.mmrl.ui.screens.moduleView.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.isNotNullOrBlank
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.model.local.State
import com.dergoogler.mmrl.model.online.isBlacklisted
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.isEmpty
import com.dergoogler.mmrl.platform.content.isValid
import com.dergoogler.mmrl.ui.component.Logo
import com.dergoogler.mmrl.ui.component.text.IconText
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalModule
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalRepo
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.providable.LocalVersionItem
import com.dergoogler.mmrl.ui.screens.moduleView.providable.LocalModuleViewModel
import com.dergoogler.mmrl.ui.screens.repository.modules.ModulesFilter
import com.ramcosta.composedestinations.generated.destinations.TypedModulesScreenDestination

@Composable
internal fun Header() {
    val userPreferences = LocalUserPreferences.current
    val viewModel = LocalModuleViewModel.current
    val repo = LocalRepo.current
    val module = LocalOnlineModule.current
    val lastVersionItem = LocalVersionItem.current
    val local = LocalModule.current
    val navigator = LocalDestinationsNavigator.current
    val density = LocalDensity.current

    val isBlacklisted by module.isBlacklisted
    val repositoryMenu = remember(userPreferences) { userPreferences.repositoryMenu }

    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (repositoryMenu.showIcon) {
            if (module.icon.isNotNullOrBlank()) {
                AsyncImage(
                    model = module.icon,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(20)),
                    contentDescription = null
                )
            } else {
                Logo(
                    icon = R.drawable.box,
                    modifier = Modifier.size(60.dp),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(20)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            IconText(
                style = MaterialTheme.typography.titleLarge,
                tint = MaterialTheme.colorScheme.surfaceTint,
                alignment = Alignment.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                text = module.name,
                resId = module.isVerified nullable R.drawable.rosette_discount_check,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                modifier = Modifier.clickable(
                    onClick = {
                        navigator.navigate(
                            TypedModulesScreenDestination(
                                type = ModulesFilter.AUTHOR,
                                title = module.author,
                                query = module.author,
                                repo = repo,
                            )
                        )
                    }
                ),
                text = module.author,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.surfaceTint),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        local.isValid {
            val ops by remember(
                userPreferences.useShellForModuleStateChange,
                it,
                it.state
            ) {
                derivedStateOf {
                    viewModel.createModuleOps(
                        userPreferences.useShellForModuleStateChange,
                        it
                    )
                }
            }

            OutlinedButton(
                enabled = viewModel.isProviderAlive && (!userPreferences.useShellForModuleStateChange || it.state != com.dergoogler.mmrl.model.local.State.REMOVE),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onClick = ops.change
            ) {
                val style = LocalTextStyle.current
                val progressSize =
                    with(density) { style.fontSize.toDp() * 1.0f }

                if (ops.isOpsRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(progressSize),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(
                            id = if (it.state == State.REMOVE) {
                                R.string.module_restore
                            } else {
                                R.string.module_remove
                            }
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        val buttonTextResId = when {
            local.isEmpty -> R.string.module_install
            lastVersionItem.isEmpty && module.versionCode > local.versionCode -> R.string.module_update
            else -> R.string.module_reinstall
        }

        Button(
            enabled = viewModel.isProviderAlive && !lastVersionItem.isEmpty && !isBlacklisted,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onClick = {
                viewModel.installConfirm = true
            },
        ) {
            Text(
                text = stringResource(id = buttonTextResId),
                maxLines = 1
            )
        }
    }
}