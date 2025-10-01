package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewModelScope
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.datastore.model.SuperUserMenu
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.platform.ksu.Profile
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.text.Collator
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class SuperUserViewModel @Inject constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    private val superUserMenu
        get() = userPreferencesRepository.data.map { it.superUserMenu }

    var isSearch by mutableStateOf(false)
        private set
    private val keyFlow = MutableStateFlow("")
    val query get() = keyFlow.asStateFlow()

    private val cacheFlow = MutableStateFlow(listOf<AppInfo>())
    private val localFlow = MutableStateFlow(listOf<AppInfo>())
    val local get() = localFlow.asStateFlow()

    private var isLoadingFlow = MutableStateFlow(false)
    val isLoading get() = isLoadingFlow.asStateFlow()

    private val profileOverrides = mutableStateMapOf<String, Profile>()

    val screenState: StateFlow<SuperUserScreenState> = localFlow
        .combine(isLoadingFlow) { items, isRefreshing ->
            SuperUserScreenState(items = items, isRefreshing = isRefreshing)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SuperUserScreenState()
        )

    init {
        providerObserver()
        dataObserver()
        keyObserver()
    }

    private fun providerObserver() {
        PlatformManager.isAliveFlow
            .onEach { if (it) fetchAppList() }
            .launchIn(viewModelScope)
    }

    private val comparator = compareBy<AppInfo> {
        when {
            it.profile != null && it.profile.allowSu -> 0
            it.profile != null && (
                    if (it.profile.allowSu) !it.profile.rootUseDefault else !it.profile.nonRootUseDefault
                    ) -> 1

            else -> 2
        }
    }.then(compareBy(Collator.getInstance(Locale.getDefault()), AppInfo::label))

    private fun dataObserver() {
        combine(cacheFlow, superUserMenu) { list, menu ->
            if (list.isEmpty()) return@combine

            val comparator = compareBy<AppInfo> {
                when {
                    it.allowSu -> 0
                    it.hasCustomProfile -> 1
                    else -> 2
                }
            }.then(compareBy(Collator.getInstance(Locale.getDefault()), AppInfo::label))

            localFlow.value = list
                .filter { menu.showSystemApps || (it.uid == 2000 || (it.packageInfo.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) == 0) }
                .sortedWith(comparator)
        }.launchIn(viewModelScope)
    }

    private fun keyObserver() {
        combine(keyFlow, cacheFlow, superUserMenu) { key, source, menu ->
            val newKey = key.trim()
            localFlow.value = source.filter {
                if (newKey.isNotBlank()) {
                    it.label.contains(newKey, true) ||
                            it.packageName.contains(newKey, true)
                } else true
            }
                .map { app ->
                    profileOverrides[app.packageName]?.let { app.copy(profile = it) } ?: app
                }
                .filter { menu.showSystemApps || (it.uid == 2000 || (it.packageInfo.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) == 0) }
                .sortedWith(comparator)
        }.launchIn(viewModelScope)
    }

    fun search(key: String) {
        keyFlow.value = key
    }

    fun openSearch() {
        isSearch = true
    }

    fun closeSearch() {
        isSearch = false
        keyFlow.value = ""
    }

    fun setSuperUserMenu(value: SuperUserMenu) = viewModelScope.launch {
        userPreferencesRepository.setSuperUserMenu(value)
    }

    private inline fun <T> T.refreshing(block: T.() -> Unit) {
        isLoadingFlow.update { true }
        block()
        isLoadingFlow.update { false }
    }

    fun fetchAppList() = viewModelScope.launch {
        refreshing {
            cacheFlow.value = withContext(Dispatchers.IO) {
                getAppList()
            }
            profileOverrides.clear()
        }
    }

    private fun getAppList(): List<AppInfo> {
        val pm = context.packageManager
        val packages =
            PlatformManager.packageManager.getInstalledPackagesAll(PlatformManager.userManager, 0)
        return packages.map { pkg ->
            val appInfo = pkg.applicationInfo!!
            val uid = appInfo.uid
            val profile = KsuNative.getAppProfile(pkg.packageName, uid)
            AppInfo(
                label = appInfo.loadLabel(pm).toString(),
                packageInfo = pkg,
                profile = profile
            )
        }.filter { it.packageName != context.packageName }
    }

    fun updateAppProfile(packageName: String, newProfile: Profile) {
        profileOverrides[packageName] = newProfile
    }

    @Parcelize
    data class AppInfo(
        val label: String,
        val packageInfo: PackageInfo,
        val profile: Profile? = null,
    ) : Parcelable {
        val packageName get() = packageInfo.packageName
        val uid get() = packageInfo.applicationInfo!!.uid
        val allowSu get() = profile?.allowSu == true
        val hasCustomProfile
            get() = profile?.let {
                if (it.allowSu) !it.rootUseDefault else !it.nonRootUseDefault
            } ?: false

        companion object {
            fun AppInfo.loadIcon(context: Context): ImageRequest {
                val drawable: Drawable? =
                    packageInfo.applicationInfo?.loadIcon(context.packageManager)
                val density = Density(context)
                val icon: Bitmap? = with(density) {
                    drawable?.toBitmap()
                }

                return ImageRequest.Builder(context)
                    .data(icon)
                    .crossfade(true)
                    .build()
            }
        }
    }

    data class SuperUserScreenState(
        val items: List<AppInfo> = emptyList(),
        val isRefreshing: Boolean = false,
    )
}
