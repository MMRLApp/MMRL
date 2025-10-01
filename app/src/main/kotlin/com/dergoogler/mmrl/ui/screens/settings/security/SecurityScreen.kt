package com.dergoogler.mmrl.ui.screens.settings.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.SwitchItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Labels
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun SecurityScreen() {
    val viewModel = LocalSettings.current
    val userPreferences = LocalUserPreferences.current

    SettingsScaffold(
        title = R.string.settings_security,
    ) {
        SwitchItem(
            checked = userPreferences.confirmReboot,
            onChange = viewModel::setConfirmReboot
        ) {
            Title(R.string.settings_reboot_protection)
            Description(R.string.settings_reboot_protection_desc)
        }

        SwitchItem(
            checked = userPreferences.blacklistAlerts,
            onChange = viewModel::setBlacklistAlerts

        ) {
            Title(R.string.settings_blacklist_alerts)
            Description(R.string.settings_blacklist_alerts_desc)
        }

        SwitchItem(
            checked = userPreferences.hideFingerprintInHome,
            onChange = viewModel::setHideFingerprintInHome
        ) {
            Title(R.string.settings_hide_fingerprint)
            Description(R.string.settings_hide_fingerprint_desc)
        }

        SwitchItem(
            checked = userPreferences.strictMode,
            onChange = viewModel::setStrictMode
        ) {
            Title(R.string.settings_strict_mode)
        }

        val isSuDisableSupported = remember {
            KsuNative.hasFeature { MINIMAL_SUPPORTED_SU_COMPAT }
        }

        var isSuDisabled by rememberSaveable {
            mutableStateOf(!KsuNative.isSuEnabled())
        }

        SwitchItem(
            enabled = isSuDisableSupported,
            checked = isSuDisabled,
            onChange = { checked ->
                val shouldEnable = !checked
                if (KsuNative.setSuEnabled(shouldEnable)) {
                    isSuDisabled = !shouldEnable
                }
            }
        ) {
            Title(R.string.settings_disable_su)
            Description(R.string.settings_disable_su_desc)

            if (!isSuDisableSupported) {
                Labels {
                    LabelItem(text = stringResource(R.string.view_module_unsupported))
                }
            }
        }
    }
}