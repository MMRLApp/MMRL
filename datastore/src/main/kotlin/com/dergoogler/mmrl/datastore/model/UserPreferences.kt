@file:OptIn(ExperimentalSerializationApi::class)

package com.dergoogler.mmrl.datastore.model

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Environment
import com.dergoogler.mmrl.datastore.BuildConfig
import com.dergoogler.mmrl.ui.theme.Colors
import com.dergoogler.mmrl.ui.theme.Colors.Companion.getColorScheme
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
data class UserPreferences(
    @ProtoNumber(1) val workingMode: WorkingMode = WorkingMode.FIRST_SETUP,
    @ProtoNumber(2) val darkMode: DarkMode = DarkMode.FollowSystem,
    @ProtoNumber(3) val themeColor: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Colors.Dynamic.id else Colors.MMRLBase.id,
    @ProtoNumber(4) val deleteZipFile: Boolean = false,
    @ProtoNumber(5) val downloadPath: String = PUBLIC_DOWNLOADS.path,
    @ProtoNumber(6) val homepage: Homepage = Homepage.Home,
    @ProtoNumber(7) val repositoryMenu: RepositoryMenu = RepositoryMenu(),
    @ProtoNumber(8) val modulesMenu: ModulesMenu = ModulesMenu(),
    @ProtoNumber(9) val repositoriesMenu: RepositoriesMenu = RepositoriesMenu(),
    @ProtoNumber(10) val useDoh: Boolean = false,
    @ProtoNumber(11) val confirmReboot: Boolean = true,
    @ProtoNumber(12) val terminalTextWrap: Boolean = false,
    @ProtoNumber(13) val datePattern: String = "d MMMM yyyy",
    @Deprecated("Replaced by RepositoryService.isActive")
    @ProtoNumber(14) val autoUpdateRepos: Boolean = false,
    @ProtoNumber(15) val autoUpdateReposInterval: Long = 6,
    @Deprecated("Replaced by ModuleService.isActive")
    @ProtoNumber(16) val checkModuleUpdates: Boolean = false,
    @ProtoNumber(17) val checkModuleUpdatesInterval: Long = 6,
    @ProtoNumber(18) val checkAppUpdates: Boolean = true,
    @ProtoNumber(19) val checkAppUpdatesPreReleases: Boolean = false,
    @ProtoNumber(20) val hideFingerprintInHome: Boolean = true,
    @ProtoNumber(21) val webUiDevUrl: String = "https://127.0.0.1:8080",
    @ProtoNumber(22) val developerMode: Boolean = false,
    @ProtoNumber(23) val useWebUiDevUrl: Boolean = false,
    @ProtoNumber(24) val useShellForModuleStateChange: Boolean = false,
    @ProtoNumber(25) val useShellForModuleAction: Boolean = true,
    @ProtoNumber(26) val clearInstallTerminal: Boolean = true,
    @ProtoNumber(27) val allowCancelInstall: Boolean = false,
    @ProtoNumber(28) val allowCancelAction: Boolean = false,
    @ProtoNumber(29) val blacklistAlerts: Boolean = true,
    @Deprecated("This is no longer used")
    @ProtoNumber(30) val injectEruda: List<String> = emptyList(),
    @Deprecated("This is no longer used")
    @ProtoNumber(31) val allowedFsModules: List<String> = emptyList(),
    @Deprecated("This is no longer used")
    @ProtoNumber(32) val allowedKsuModules: List<String> = emptyList(),
    @Deprecated("Replaced by ProviderService.isActive")
    @ProtoNumber(33) val useProviderAsBackgroundService: Boolean = false,
    @ProtoNumber(34) val strictMode: Boolean = true,
    @ProtoNumber(35) val enableErudaConsole: Boolean = false,
    @ProtoNumber(36) val enableToolbarEvents: Boolean = true,
    @ProtoNumber(37) val webuiEngine: WebUIEngine = WebUIEngine.PREFER_MODULE,
    @ProtoNumber(38) val showTerminalLineNumbers: Boolean = true,
    @ProtoNumber(39) val devAlwaysShowUpdateAlert: Boolean = false,
    @ProtoNumber(40) val webuixPackageName: String = "com.dergoogler.mmrl.wx${if (BuildConfig.DEBUG) ".debug" else ""}",
    @ProtoNumber(41) val enableBlur: Boolean = true,
    @ProtoNumber(42) val hideBottomBarLabels: Boolean = false,
    @ProtoNumber(43) val superUserMenu: SuperUserMenu = SuperUserMenu(),
) {
    fun isDarkMode() = when (darkMode) {
        DarkMode.AlwaysOff -> false
        DarkMode.AlwaysOn -> true
        DarkMode.FollowSystem -> isSystemInDarkTheme()
    }

    fun colorScheme(context: Context) = context.getColorScheme(themeColor, isDarkMode())

    private fun isSystemInDarkTheme(): Boolean {
        val uiMode = Resources.getSystem().configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun encodeTo(output: OutputStream) = output.write(
        ProtoBuf.encodeToByteArray(this)
    )

    @OptIn(ExperimentalContracts::class)
    fun developerMode(
        also: UserPreferences.() -> Boolean,
    ): Boolean {
        contract {
            callsInPlace(also, InvocationKind.AT_MOST_ONCE)
        }

        return developerMode && also()
    }

    companion object {
        val PUBLIC_DOWNLOADS: File = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

        @OptIn(ExperimentalSerializationApi::class)
        fun decodeFrom(input: InputStream): UserPreferences =
            ProtoBuf.decodeFromByteArray(input.readBytes())
    }
}
