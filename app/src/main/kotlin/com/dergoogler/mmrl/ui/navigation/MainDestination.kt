package com.dergoogler.mmrl.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.ui.remember.rememberIsRoot
import com.dergoogler.mmrl.utils.isManager
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ModulesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.RepositoriesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SuperUserScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

//@NavHostGraph
//annotation class MainGraph

enum class MainDestination(
    val direction: DirectionDestinationSpec,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val iconFilled: Int,
    val requiresRoot: Boolean,
    val requiresKernel: Boolean,
) {
    Home(
        direction = HomeScreenDestination,
        label = R.string.page_home,
        icon = R.drawable.home,
        iconFilled = R.drawable.home_filled,
        requiresRoot = false,
        requiresKernel = false
    ),

    SuperUser(
        direction = SuperUserScreenDestination,
        label = R.string.page_superuser,
        icon = R.drawable.user,
        iconFilled = R.drawable.user_filled,
        requiresRoot = true,
        requiresKernel = true
    ),

    Repository(
        direction = RepositoriesScreenDestination,
        label = R.string.page_repositorys,
        icon = R.drawable.cloud,
        iconFilled = R.drawable.cloud_filled,
        requiresRoot = false,
        requiresKernel = false
    ),

    Modules(
        direction = ModulesScreenDestination,
        label = R.string.page_modules,
        icon = R.drawable.keyframes,
        iconFilled = R.drawable.keyframes_filled,
        requiresRoot = true,
        requiresKernel = false
    ),

    Settings(
        direction = SettingsScreenDestination,
        label = R.string.page_settings,
        icon = R.drawable.settings,
        iconFilled = R.drawable.settings_filled,
        requiresRoot = false,
        requiresKernel = false
    ),
}

@get:Composable
val MainDestination.isAccessible: Boolean
    get() {
        val isRoot = rememberIsRoot()
        if (this.requiresRoot && !isRoot) return false
        if (this.requiresKernel && !(KsuNative.isManager && isRoot)) return false
        return true
    }