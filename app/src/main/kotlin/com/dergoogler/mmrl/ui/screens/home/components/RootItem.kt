package com.dergoogler.mmrl.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.local.FeaturedManager
import com.dergoogler.mmrl.model.local.FeaturedManager.Companion.name
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.remember.versionCode
import dev.mmrlx.compose.ui.scaffold.PositionLayout
import dev.mmrlx.compose.ui.theme.MMRLXTheme

@Composable
internal fun RootItem() {
    val userPreferences = LocalUserPreferences.current
    val shape = MMRLXTheme.shapes.extraLarge
    val isAlive = PlatformManager.isAlive

    val manager =
        FeaturedManager.managers.find { userPreferences.workingMode == it.workingMode }

    PositionLayout(
        modifier = Modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
    ) {
        if (userPreferences.developerMode) {
            Surface(
                shape =
                    RoundedCornerShape(
                        topStart = CornerSize(0.dp),
                        topEnd = shape.topEnd,
                        bottomStart = CornerSize(15.dp),
                        bottomEnd = CornerSize(0.dp),
                    ),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                modifier =
                    Modifier
                        .absolute(Alignment.TopEnd),
            ) {
                Text(
                    text = "USER!DEV",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .relative()
                    .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(45.dp),
                painter =
                    painterResource(
                        id = getManagerLogo(isAlive, manager),
                    ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text =
                        if (isAlive) {
                            stringResource(
                                id = R.string.settings_root_access,
                                stringResource(id = R.string.settings_root_granted),
                            )
                        } else {
                            stringResource(
                                id = R.string.settings_root_access,
                                stringResource(id = R.string.settings_root_none),
                            )
                        },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Text(
                    text =
                        if (isAlive) {
                            stringResource(
                                id = R.string.settings_root_provider,
                                if (manager?.name == null) {
                                    "???"
                                } else {
                                    manager.name
                                },
                            )
                        } else {
                            stringResource(
                                id = R.string.settings_root_provider,
                                stringResource(id = R.string.settings_root_not_available),
                            )
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Text(
                    text =
                        if (isAlive) {
                            stringResource(
                                id = R.string.settings_root_version,
                                versionCode,
                            )
                        } else {
                            stringResource(
                                id = R.string.settings_root_version,
                                stringResource(id = R.string.settings_root_none),
                            )
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

private fun getManagerLogo(
    isAlive: Boolean,
    manager: FeaturedManager?,
): Int {
    if (!isAlive) {
        return R.drawable.alert_circle_filled
    }

    if (manager == null) {
        return R.drawable.circle_check_filled
    }

    return manager.icon
}
