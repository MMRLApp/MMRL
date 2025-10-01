package com.dergoogler.mmrl.ui.screens.appProfile.remember

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.platform.SePolicy
import com.dergoogler.mmrl.platform.SePolicy.getSepolicy
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.platform.ksu.Profile
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.providable.LocalSuperUserViewModel
import com.dergoogler.mmrl.viewmodel.SuperUserViewModel
import kotlinx.coroutines.launch

@Composable
fun rememberProfileChange(info: SuperUserViewModel.AppInfo): Pair<Profile, (Profile) -> Unit> {
    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val viewModel = LocalSuperUserViewModel.current

    val initialProfile = remember(info.packageName, info.uid) {
        val profile = KsuNative.getAppProfile(info.packageName, info.uid)
        if (profile.allowSu) {
            profile.rules = getSepolicy(info.packageName)
        }
        profile
    }

    var profile by rememberSaveable(info.packageName, info.uid) {
        mutableStateOf(initialProfile)
    }

    // Cache string resources to avoid recomposition
    val errorMessages = remember {
        ErrorMessages(
            failToUpdateAppProfile = R.string.failed_to_update_app_profile,
            failToUpdateSepolicy = R.string.failed_to_update_sepolicy,
            suNotAllowed = R.string.su_not_allowed
        )
    }

    val updateProfile: (Profile) -> Unit = remember(info, snackBarHost, scope, viewModel) {
        { newProfile ->
            scope.launch {
                try {
                    val result = updateProfileSafely(
                        newProfile, info, errorMessages, snackBarHost,
                    )
                    if (result.isSuccess) {
                        profile = newProfile
                        viewModel.updateAppProfile(info.packageName, newProfile)
                    }
                } catch (e: Exception) {
                    Log.e("ProfileChange", "Error updating profile", e)
                    snackBarHost.showSnackbar("An unexpected error occurred")
                }
            }
        }
    }

    return profile to updateProfile
}

private data class ErrorMessages(
    val failToUpdateAppProfile: Int,
    val failToUpdateSepolicy: Int,
    val suNotAllowed: Int,
)

private sealed class UpdateResult {
    object Success : UpdateResult()
    data class Error(val messageRes: Int, val args: List<Any>) : UpdateResult()

    val isSuccess: Boolean get() = this is Success
}

private suspend fun updateProfileSafely(
    profile: Profile,
    info: SuperUserViewModel.AppInfo,
    errorMessages: ErrorMessages,
    snackBarHost: SnackbarHostState,
): UpdateResult {
    if (profile.allowSu) {
        // Validate system UID restrictions
        if (isSystemUidForbidden(info.uid)) {
            snackBarHost.showSnackbar(
                // You'll need to resolve string resource here or pass context
                "SU not allowed for system UID ${info.uid}"
            )
            return UpdateResult.Error(errorMessages.suNotAllowed, listOf(info.label))
        }

        // Handle SELinux policy updates
        if (shouldUpdateSepolicy(profile)) {
            val sepolicyResult = SePolicy.setSePolicy(profile.name, profile.rules)
            if (!sepolicyResult) {
                snackBarHost.showSnackbar("Failed to update SEPolicy for ${info.label}")
                return UpdateResult.Error(errorMessages.failToUpdateSepolicy, listOf(info.label))
            }
        }
    }

    // Update app profile
    val profileUpdateSuccess = KsuNative.setAppProfile(profile)
    if (!profileUpdateSuccess) {
        snackBarHost.showSnackbar("Failed to update app profile for ${info.label}")
        return UpdateResult.Error(errorMessages.failToUpdateAppProfile, listOf(info.uid))
    }

    return UpdateResult.Success
}

private fun isSystemUidForbidden(uid: Int): Boolean {
    // sync with allowlist.c - forbid_system_uid
    return uid < 2000 && uid != 1000
}

private fun shouldUpdateSepolicy(profile: Profile): Boolean {
    return !profile.rootUseDefault && profile.rules.isNotEmpty()
}

val LocalProfileChange = staticCompositionLocalOf<Pair<Profile, (Profile) -> Unit>> {
    error("CompositionLocal LocalProfileChange not present")
}