package com.dergoogler.mmrl.ui.screens.moduleView.sections

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.ifNotNullOrBlank
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.model.online.isValid
import com.dergoogler.mmrl.platform.content.isValid
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toFormattedFileSize
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.CollapseItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Icon
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalModule
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.providable.LocalVersionItem
import com.dergoogler.mmrl.ui.screens.moduleView.items.LicenseItem
import com.dergoogler.mmrl.ui.screens.moduleView.providable.LocalModuleViewModel
import com.dergoogler.mmrl.utils.toFormattedDateSafely

@Composable
internal fun Information() {
    val viewModel = LocalModuleViewModel.current
    val userPreferences = LocalUserPreferences.current
    val module = LocalOnlineModule.current
    val lastVersionItem = LocalVersionItem.current
    val local = LocalModule.current

    userPreferences.developerMode.takeTrue {
        ModuleInfoListItem(
            title = R.string.view_module_module_id,
            desc = module.id
        )
    }

    module.license.ifNotNullOrBlank {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.view_module_license)
            )
            LicenseItem(licenseId = it)
        }
    }

    ModuleInfoListItem(
        title = R.string.view_module_version,
        desc = "${module.version} (${module.versionCode})"
    )

    lastVersionItem.isValid {
        ModuleInfoListItem(
            title = R.string.view_module_last_updated,
            desc = it.timestamp.toFormattedDateSafely
        )
    }

    module.size?.let {
        ModuleInfoListItem(
            title = R.string.view_module_file_size,
            desc = it.toFormattedFileSize()
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.view_module_provided_by)
        )

        Text(
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.surfaceTint),
            modifier = Modifier.clickable(
                onClick = { viewModel.viewTrackBottomSheet = true }
            ),
            text = viewModel.repo.name,
        )
    }

    module.manager(viewModel.platform).min?.let {
        ModuleInfoListItem(
            title = R.string.view_module_required_root_version,
            desc = it.toString()
        )
    }

    module.minApi?.let {
        ModuleInfoListItem(
            title = R.string.view_module_required_os,
            desc = stringResource(
                R.string.view_module_required_os_value, when (it) {
                    Build.VERSION_CODES.JELLY_BEAN -> "4.1"
                    Build.VERSION_CODES.JELLY_BEAN_MR1 -> "4.2"
                    Build.VERSION_CODES.JELLY_BEAN_MR2 -> "4.3"
                    Build.VERSION_CODES.KITKAT -> "4.4"
                    Build.VERSION_CODES.KITKAT_WATCH -> "4.4"
                    Build.VERSION_CODES.LOLLIPOP -> "5.0"
                    Build.VERSION_CODES.LOLLIPOP_MR1 -> "5.1"
                    Build.VERSION_CODES.M -> "6.0"
                    Build.VERSION_CODES.N -> "7.0"
                    Build.VERSION_CODES.N_MR1 -> "7.1"
                    Build.VERSION_CODES.O -> "8.0"
                    Build.VERSION_CODES.O_MR1 -> "8.1"
                    Build.VERSION_CODES.P -> "9.0"
                    Build.VERSION_CODES.Q -> "10"
                    Build.VERSION_CODES.R -> "11"
                    Build.VERSION_CODES.S -> "12"
                    Build.VERSION_CODES.S_V2 -> "12"
                    Build.VERSION_CODES.TIRAMISU -> "13"
                    Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> "14"
                    else -> "[Sdk: $it]"
                }
            )
        )
    }

    module.track.added?.let {
        ModuleInfoListItem(
            title = R.string.view_module_added_on,
            desc = it.toFormattedDateSafely
        )
    }


    local.isValid { loc ->
        List(
            contentPadding = PaddingValues(
                vertical = 8.dp,
                horizontal = 16.dp
            )
        ) {
            CollapseItem(
                meta = { icon, rotation ->
                    Title(
                        id = R.string.module_installed,
                        styleTransform = {
                            val newStyle =
                                MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.surfaceTint)

                            it.merge(newStyle)
                        }
                    )
                    Icon(
                        slot = ListItemSlot.End,
                        modifier = Modifier
                            .graphicsLayer(rotationZ = rotation),
                        painter = painterResource(id = icon),
                    )
                }
            ) {
                userPreferences.developerMode.takeTrue {
                    ModuleInfoListItem(
                        title = R.string.view_module_module_id,
                        desc = loc.id.toString()
                    )
                }

                ModuleInfoListItem(
                    title = R.string.view_module_version,
                    desc = "${loc.version} (${loc.versionCode})"
                )

                ModuleInfoListItem(
                    title = R.string.view_module_last_updated,
                    desc = loc.lastUpdated.toFormattedDateSafely
                )
            }
        }
    }
}

@Composable
private fun ModuleInfoListItem(
    @StringRes title: Int,
    desc: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    infoCanDiffer: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            style = style.copy(color = MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f),
            text = stringResource(id = title) + if (infoCanDiffer) " *" else ""
        )
        Text(
            style = style,
            text = desc
        )
    }
}