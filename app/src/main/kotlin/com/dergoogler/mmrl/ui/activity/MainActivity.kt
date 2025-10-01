package com.dergoogler.mmrl.ui.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.database.entity.Repo.Companion.toRepo
import com.dergoogler.mmrl.datastore.model.WorkingMode
import com.dergoogler.mmrl.datastore.model.WorkingMode.Companion.isRoot
import com.dergoogler.mmrl.datastore.model.WorkingMode.Companion.isSetup
import com.dergoogler.mmrl.network.NetworkUtils
import com.dergoogler.mmrl.ui.activity.terminal.action.ActionActivity
import com.dergoogler.mmrl.ui.activity.terminal.install.InstallActivity
import com.dergoogler.mmrl.ui.screens.main.MainScreen
import com.dergoogler.mmrl.utils.BlurUtil
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : MMRLComponentActivity() {
    private var isLoading by mutableStateOf(true)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override val requirePermissions = listOf(Manifest.permission.POST_NOTIFICATIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { isLoading }

        setBaseContent {
            val userPreferences by userPreferencesRepository.data
                .collectAsStateWithLifecycle(initialValue = null)

            val preferences = if (userPreferences == null) {
                return@setBaseContent
            } else {
                isLoading = false
                checkNotNull(userPreferences)
            }


            LaunchedEffect(userPreferences) {
                if (preferences.workingMode.isSetup) {
                    Timber.d("add default repository")
                    localRepository.insertRepo(Const.DEMO_REPO_URL.toRepo())
                }

                modulesRepository.getBlacklist()

                NetworkUtils.setEnableDoh(preferences.useDoh)

                if (!BlurUtil.isBlurSupported()) {
                    setEnableBlur(false)
                }

                setActivityEnabled<InstallActivity>(preferences.workingMode.isRoot)
                setActivityEnabled<ActionActivity>(preferences.workingMode.isRoot)
            }

            Crossfade(
                targetState = preferences.workingMode.isSetup,
                label = "MainActivity"
            ) { isSetup ->
                if (isSetup) {
                    SetupScreen(
                        setWorkingMode = ::setWorkingMode
                    )
                } else {
                    MainScreen()
                }
            }
        }
    }

    private fun setWorkingMode(value: WorkingMode) {
        lifecycleScope.launch {
            userPreferencesRepository.setWorkingMode(value)
        }
    }

    private fun setEnableBlur(value: Boolean) {
        lifecycleScope.launch {
            userPreferencesRepository.setEnableBlur(value)
        }
    }
}