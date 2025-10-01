package com.dergoogler.mmrl.ui.screens.settings.appearance.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.ScaffoldDefaults
import com.dergoogler.mmrl.ui.component.SettingsScaffold
import com.dergoogler.mmrl.ui.providable.LocalSettings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.screens.settings.appearance.items.DarkModeItem
import com.dergoogler.mmrl.ui.screens.settings.appearance.items.ExampleItem
import com.dergoogler.mmrl.ui.screens.settings.appearance.items.ThemePaletteItem
import com.dergoogler.mmrl.ui.screens.settings.appearance.items.TitleItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun AppThemeScreen() {
    val userPreferences = LocalUserPreferences.current
    val viewModel = LocalSettings.current

    SettingsScaffold(
        modifier = ScaffoldDefaults.settingsScaffoldScrollModifier,
        title = R.string.settings_app_theme
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExampleItem()
        }

        TitleItem(text = stringResource(id = R.string.app_theme_palette))
        ThemePaletteItem(
            themeColor =  userPreferences.themeColor,
            isDarkMode = userPreferences.isDarkMode(),
            onChange = viewModel::setThemeColor
        )

        TitleItem(text = stringResource(id = R.string.app_theme_dark_theme))
        DarkModeItem(
            darkMode = userPreferences.darkMode,
            onChange = viewModel::setDarkTheme
        )
    }
}