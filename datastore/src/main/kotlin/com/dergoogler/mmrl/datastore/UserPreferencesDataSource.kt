package com.dergoogler.mmrl.datastore

import androidx.datastore.core.DataStore
import com.dergoogler.mmrl.datastore.model.DarkMode
import com.dergoogler.mmrl.datastore.model.Homepage
import com.dergoogler.mmrl.datastore.model.ModulesMenu
import com.dergoogler.mmrl.datastore.model.RepositoriesMenu
import com.dergoogler.mmrl.datastore.model.RepositoryMenu
import com.dergoogler.mmrl.datastore.model.SuperUserMenu
import com.dergoogler.mmrl.datastore.model.UserPreferences
import com.dergoogler.mmrl.datastore.model.WebUIEngine
import com.dergoogler.mmrl.datastore.model.WorkingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserPreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val data get() = userPreferences.data

    suspend fun setWorkingMode(value: WorkingMode) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                workingMode = value
            )
        }
    }

    suspend fun setDarkTheme(value: DarkMode) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                darkMode = value
            )
        }
    }

    suspend fun setThemeColor(value: Int) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                themeColor = value
            )
        }
    }

    suspend fun setDeleteZipFile(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                deleteZipFile = value
            )
        }
    }

    suspend fun setUseDoh(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                useDoh = value
            )
        }
    }

    suspend fun setDownloadPath(value: String) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                downloadPath = value
            )
        }
    }

    suspend fun setConfirmReboot(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                confirmReboot = value
            )
        }
    }

    suspend fun setTerminalTextWrap(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                terminalTextWrap = value
            )
        }
    }

    suspend fun setDatePattern(value: String) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                datePattern = value
            )
        }
    }

    suspend fun setAutoUpdateRepos(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                autoUpdateRepos = value
            )
        }
    }

    suspend fun setAutoUpdateReposInterval(value: Long) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                autoUpdateReposInterval = value
            )
        }
    }

    suspend fun setCheckModuleUpdates(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                checkModuleUpdates = value
            )
        }
    }

    suspend fun setEnableBlur(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                enableBlur = value
            )
        }
    }

    suspend fun setCheckModuleUpdatesInterval(value: Long) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                checkModuleUpdatesInterval = value
            )
        }
    }

    suspend fun setCheckAppUpdates(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                checkAppUpdates = value
            )
        }
    }

    suspend fun setCheckAppUpdatesPreReleases(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                checkAppUpdatesPreReleases = value
            )
        }
    }

    suspend fun setHideFingerprintInHome(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                hideFingerprintInHome = value
            )
        }
    }

    suspend fun setStrictMode(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                strictMode = value
            )
        }
    }

    suspend fun setHomepage(value: Homepage) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                homepage = value
            )
        }
    }

    suspend fun setWebUiDevUrl(value: String) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                webUiDevUrl = value
            )
        }
    }

    suspend fun setWebuixPackageName(value: String) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                webuixPackageName = value
            )
        }
    }

    suspend fun setDeveloperMode(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                developerMode = value
            )
        }
    }

    suspend fun setUseWebUiDevUrl(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                useWebUiDevUrl = value
            )
        }
    }

    suspend fun setUseShellForModuleStateChange(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                useShellForModuleStateChange = value
            )
        }
    }

    suspend fun setUseShellForModuleAction(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                useShellForModuleAction = value
            )
        }
    }

    suspend fun setClearInstallTerminal(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                clearInstallTerminal = value
            )
        }
    }

    suspend fun setAllowCancelInstall(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                allowCancelInstall = value
            )
        }
    }

    suspend fun setAllowCancelAction(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                allowCancelAction = value
            )
        }
    }

    suspend fun setBlacklistAlerts(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                blacklistAlerts = value
            )
        }
    }

    suspend fun setInjectEruda(value: List<String>) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                injectEruda = value
            )
        }
    }

    suspend fun setAllowedFsModules(value: List<String>) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                allowedFsModules = value
            )
        }
    }

    suspend fun setAllowedKsuModules(value: List<String>) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                allowedKsuModules = value
            )
        }
    }

    suspend fun setRepositoryMenu(value: RepositoryMenu) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                repositoryMenu = value
            )
        }
    }

    suspend fun setSuperUserMenu(value: SuperUserMenu) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                superUserMenu = value
            )
        }
    }

    suspend fun setRepositoriesMenu(value: RepositoriesMenu) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                repositoriesMenu = value
            )
        }
    }

    suspend fun setModulesMenu(value: ModulesMenu) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                modulesMenu = value
            )
        }
    }

    suspend fun setEnableEruda(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                enableErudaConsole = value
            )
        }
    }

    suspend fun setEnableToolbarEvents(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                enableToolbarEvents = value
            )
        }
    }

    suspend fun setShowTerminalLineNumbers(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                showTerminalLineNumbers = value
            )
        }
    }

    suspend fun setDevAlwaysShowUpdateAlert(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                devAlwaysShowUpdateAlert = value
            )
        }
    }

    suspend fun setHideBottomBarLabels(value: Boolean) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                hideBottomBarLabels = value
            )
        }
    }

    suspend fun setWebUIEngine(value: WebUIEngine) = withContext(Dispatchers.IO) {
        userPreferences.updateData {
            it.copy(
                webuiEngine = value
            )
        }
    }
}