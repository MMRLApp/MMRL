package com.dergoogler.mmrl.ui.screens.moduleView.sections

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.online.hasBlacklist
import com.dergoogler.mmrl.model.online.hasValidMessage
import com.dergoogler.mmrl.ui.component.Alert
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.screens.moduleView.providable.LocalModuleViewModel
import com.dergoogler.mmrl.ui.screens.settings.blacklist.items.BlacklistBottomSheet

private val alertPadding = PaddingValues(horizontal = 16.dp)

@Composable
internal fun Alerts() {
    val viewModel = LocalModuleViewModel.current
    val module = LocalOnlineModule.current

    val manager = remember(module) {
        module.manager(viewModel.platform)
    }

    module.hasBlacklist {
        var open by remember { mutableStateOf(false) }
        if (open) {
            BlacklistBottomSheet(
                module = it,
                onClose = { open = false })
        }

        Alert(
            icon = R.drawable.alert_circle_filled,
            title = stringResource(R.string.blacklisted),
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            textColor = MaterialTheme.colorScheme.onErrorContainer,
            message = stringResource(R.string.blacklisted_desc),
            outsideContentPadding = alertPadding,
        )
    }

    manager.isNotSupportedRootVersion(viewModel.versionCode) { min ->
        if (min == -1) {
            Alert(
                title = stringResource(id = R.string.view_module_unsupported),
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer,
                message = stringResource(id = R.string.view_module_unsupported_desc),
                outsideContentPadding = alertPadding,
            )
        } else {
            Alert(
                title = stringResource(id = R.string.view_module_low_root_version),
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                message = stringResource(id = R.string.view_module_low_root_version_desc),
                outsideContentPadding = alertPadding,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    manager.isNotSupportedDevice {
        Alert(
            title = stringResource(id = R.string.view_module_unsupported_device),
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            textColor = MaterialTheme.colorScheme.onErrorContainer,
            message = stringResource(id = R.string.view_module_unsupported_device_desc),
            outsideContentPadding = alertPadding,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    manager.isNotSupportedArch {
        Alert(
            title = stringResource(id = R.string.view_module_unsupported_arch),
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            textColor = MaterialTheme.colorScheme.onErrorContainer,
            message = stringResource(id = R.string.view_module_unsupported_arch_desc),
            outsideContentPadding = alertPadding,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    module.note.hasValidMessage {
        if (it.hasTitle && it.isDeprecated) {
            Alert(
                icon = R.drawable.alert_triangle,
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer,
                title = it.title,
                message = it.message!!,
                outsideContentPadding = alertPadding,
            )
        } else {
            Alert(
                title = it.title,
                message = it.message!!,
                outsideContentPadding = alertPadding,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}