package com.dergoogler.mmrl.datastore

import com.dergoogler.mmrl.datastore.model.DarkMode
import com.dergoogler.mmrl.datastore.model.Homepage
import com.dergoogler.mmrl.datastore.model.ModulesMenu
import com.dergoogler.mmrl.datastore.model.RepositoriesMenu
import com.dergoogler.mmrl.datastore.model.RepositoryMenu
import com.dergoogler.mmrl.datastore.model.SuperUserMenu
import com.dergoogler.mmrl.datastore.model.WebUIEngine
import com.dergoogler.mmrl.datastore.model.WorkingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDataSource: UserPreferencesDataSource
) {
    val data get() = userPreferencesDataSource.data

    suspend fun setWorkingMode(value: WorkingMode) = userPreferencesDataSource.setWorkingMode(value)

    suspend fun setDarkTheme(value: DarkMode) = userPreferencesDataSource.setDarkTheme(value)

    suspend fun setThemeColor(value: Int) = userPreferencesDataSource.setThemeColor(value)

    suspend fun setDeleteZipFile(value: Boolean) = userPreferencesDataSource.setDeleteZipFile(value)

    suspend fun setUseDoh(value: Boolean) = userPreferencesDataSource.setUseDoh(value)

    suspend fun setDownloadPath(value: String) = userPreferencesDataSource.setDownloadPath(value)

    suspend fun setConfirmReboot(value: Boolean) = userPreferencesDataSource.setConfirmReboot(value)

    suspend fun setTerminalTextWrap(value: Boolean) =
        userPreferencesDataSource.setTerminalTextWrap(value)

    suspend fun setDatePattern(value: String) = userPreferencesDataSource.setDatePattern(value)

    suspend fun setAutoUpdateRepos(value: Boolean) =
        userPreferencesDataSource.setAutoUpdateRepos(value)

    suspend fun setAutoUpdateReposInterval(value: Long) =
        userPreferencesDataSource.setAutoUpdateReposInterval(value)

    suspend fun setCheckModuleUpdates(value: Boolean) =
        userPreferencesDataSource.setCheckModuleUpdates(value)

    suspend fun setEnableBlur(value: Boolean) =
        userPreferencesDataSource.setEnableBlur(value)

    suspend fun setCheckModuleUpdatesInterval(value: Long) =
        userPreferencesDataSource.setCheckModuleUpdatesInterval(value)

    suspend fun setCheckAppUpdates(value: Boolean) =
        userPreferencesDataSource.setCheckAppUpdates(value)

    suspend fun setCheckAppUpdatesPreReleases(value: Boolean) =
        userPreferencesDataSource.setCheckAppUpdatesPreReleases(value)

    suspend fun setHideFingerprintInHome(value: Boolean) =
        userPreferencesDataSource.setHideFingerprintInHome(value)

    suspend fun setStrictMode(value: Boolean) =
        userPreferencesDataSource.setStrictMode(value)

    suspend fun setHomepage(value: Homepage) =
        userPreferencesDataSource.setHomepage(value)

    suspend fun setWebUiDevUrl(value: String) =
        userPreferencesDataSource.setWebUiDevUrl(value)

    suspend fun setWebuixPackageName(value: String) =
        userPreferencesDataSource.setWebuixPackageName(value)

    suspend fun setDeveloperMode(value: Boolean) =
        userPreferencesDataSource.setDeveloperMode(value)

    suspend fun setUseWebUiDevUrl(value: Boolean) =
        userPreferencesDataSource.setUseWebUiDevUrl(value)

    suspend fun setUseShellForModuleStateChange(value: Boolean) =
        userPreferencesDataSource.setUseShellForModuleStateChange(value)

    suspend fun setUseShellForModuleAction(value: Boolean) =
        userPreferencesDataSource.setUseShellForModuleAction(value)

    suspend fun setClearInstallTerminal(value: Boolean) =
        userPreferencesDataSource.setClearInstallTerminal(value)

    suspend fun setAllowCancelInstall(value: Boolean) =
        userPreferencesDataSource.setAllowCancelInstall(value)

    suspend fun setAllowCancelAction(value: Boolean) =
        userPreferencesDataSource.setAllowCancelAction(value)

    suspend fun setBlacklistAlerts(value: Boolean) =
        userPreferencesDataSource.setBlacklistAlerts(value)

    suspend fun setInjectEruda(value: List<String>) =
        userPreferencesDataSource.setInjectEruda(value)

    suspend fun setAllowedFsModules(value: List<String>) =
        userPreferencesDataSource.setAllowedFsModules(value)

    suspend fun setAllowedKsuModules(value: List<String>) =
        userPreferencesDataSource.setAllowedKsuModules(value)

    suspend fun setRepositoryMenu(value: RepositoryMenu) =
        userPreferencesDataSource.setRepositoryMenu(value)

    suspend fun setSuperUserMenu(value: SuperUserMenu) =
        userPreferencesDataSource.setSuperUserMenu(value)

    suspend fun setRepositoriesMenu(value: RepositoriesMenu) =
        userPreferencesDataSource.setRepositoriesMenu(value)

    suspend fun setModulesMenu(value: ModulesMenu) =
        userPreferencesDataSource.setModulesMenu(value)

    suspend fun setEnableEruda(value: Boolean) =
        userPreferencesDataSource.setEnableEruda(value)

    suspend fun setShowTerminalLineNumbers(value: Boolean) =
        userPreferencesDataSource.setShowTerminalLineNumbers(value)

    suspend fun setDevAlwaysShowUpdateAlert(value: Boolean) =
        userPreferencesDataSource.setDevAlwaysShowUpdateAlert(value)

    suspend fun setEnableToolbarEvents(value: Boolean) =
        userPreferencesDataSource.setEnableToolbarEvents(value)

    suspend fun setHideBottomBarLabels(value: Boolean) =
        userPreferencesDataSource.setHideBottomBarLabels(value)

    suspend fun setWebUIEngine(value: WebUIEngine) =
        userPreferencesDataSource.setWebUIEngine(value)
}