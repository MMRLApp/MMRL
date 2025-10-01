package com.dergoogler.mmrl.ui.screens.home.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.model.local.FeaturedManager
import com.dergoogler.mmrl.model.local.FeaturedManager.Companion.name
import com.dergoogler.mmrl.platform.PlatformManager.isAlive
import com.dergoogler.mmrl.platform.PlatformManager.platform
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.component.text.TextRow
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.remember.isLkmMode
import com.dergoogler.mmrl.ui.remember.versionCode

@Composable
internal fun RootItem(
    developerMode: Boolean = false,
) {
    val userPreferences = LocalUserPreferences.current

    val manager =
        FeaturedManager.managers.find { userPreferences.workingMode == it.workingMode }

    Card(
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        developerMode.takeTrue {
            Surface(
                shape = RoundedCornerShape(
                    topEnd = 20.dp,
                    bottomStart = 15.dp,
                    bottomEnd = 0.dp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .absolute(Alignment.TopEnd)
            ) {
                Text(
                    text = "USER!DEV",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .relative()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(45.dp),
                painter = painterResource(
                    id = getManagerLogo(isAlive, manager)
                ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextRow(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = (developerMode && platform.isKernelSuOrNext) nullable {
                        LabelItem(
                            text = when (isLkmMode) {
                                null -> "LTS"
                                true -> "LKM"
                                else -> "GKI"
                            }
                        )
                    },
                ) {
                    Text(
                        text = if (isAlive) {
                            stringResource(
                                id = R.string.settings_root_access,
                                stringResource(id = R.string.settings_root_granted)
                            )
                        } else {
                            stringResource(
                                id = R.string.settings_root_access,
                                stringResource(id = R.string.settings_root_none)
                            )
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Text(
                    text = if (isAlive) {
                        stringResource(
                            id = R.string.settings_root_provider, manager.name
                        )
                    } else {
                        stringResource(
                            id = R.string.settings_root_provider,
                            stringResource(id = R.string.settings_root_not_available)
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = if (isAlive) {
                        stringResource(
                            id = R.string.settings_root_version,
                            versionCode
                        )
                    } else {
                        stringResource(
                            id = R.string.settings_root_version,
                            stringResource(id = R.string.settings_root_none)
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

private fun getManagerLogo(isAlive: Boolean, manager: FeaturedManager?): Int {
    if (!isAlive) {
        return R.drawable.alert_circle_filled
    }


    if (manager == null) {
        return R.drawable.circle_check_filled
    }

    return manager.icon
}