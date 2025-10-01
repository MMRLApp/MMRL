package com.dergoogler.mmrl.ui.screens.appProfile

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.ui.component.LocalScreen
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Item
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.RadioDialogItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.SwitchItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Icon
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Start
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.component.toolbar.BlurNavigateUpToolbar
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalSnackbarHost
import com.dergoogler.mmrl.ui.screens.appProfile.items.RootProfileConfig
import com.dergoogler.mmrl.ui.screens.appProfile.remember.LocalProfileChange
import com.dergoogler.mmrl.ui.screens.appProfile.remember.rememberProfileChange
import com.dergoogler.mmrl.viewmodel.SuperUserViewModel
import com.dergoogler.mmrl.viewmodel.SuperUserViewModel.AppInfo.Companion.loadIcon
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun AppProfileScreen(
    appInfo: SuperUserViewModel.AppInfo,
) = LocalScreenProvider {
    val context = LocalContext.current
    val snackBarHost = LocalSnackbarHost.current
    val bottomBarPaddingValues = LocalMainScreenInnerPaddings.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val (profile, onProfileChange) = rememberProfileChange(appInfo)

    val icon = remember {
        appInfo.loadIcon(context)
    }

    CompositionLocalProvider(
        LocalProfileChange provides (profile to onProfileChange)
    ) {
        Scaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                BlurNavigateUpToolbar(
                    fade = true,
                    fadeDistance = 50f,
                    navigator = LocalDestinationsNavigator.current,
                    title = stringResource(R.string.profile),
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHost = {
                LocalScreen.SnackbarHost()
            },
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        ) { paddingValues ->
            val isRootGranted = profile.allowSu

            var mode by rememberSaveable(profile, isRootGranted) {
                val initialMode = if (isRootGranted) {
                    if (profile.rootUseDefault) Mode.Default else Mode.Custom
                } else {
                    if (profile.nonRootUseDefault) Mode.Default else Mode.Custom
                }
                mutableStateOf(initialMode)
            }

            List(
                modifier = Modifier
                    .hazeSource(LocalHazeState.current)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(paddingValues.calculateTopPadding()))

                Item {
                    Title(appInfo.label)
                    Description(appInfo.packageName)
                    Start {
                        AsyncImage(
                            model = icon,
                            contentDescription = appInfo.label,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                SwitchItem(
                    checked = isRootGranted,
                    onChange = {
                        onProfileChange(profile.copy(allowSu = it))
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.square_root_2)
                    )
                    Title(R.string.allow_root_access)
                    Description(R.string.allow_root_access_desc)
                }

                RadioDialogItem(
                    selection = mode,
                    options = mode.dialogOptions,
                    onConfirm = {
                        if (isRootGranted) {
                            if (it.value == Mode.Default || it.value == Mode.Custom) {
                                onProfileChange(profile.copy(rootUseDefault = it.value == Mode.Default))
                            }

                            mode = it.value

                            return@RadioDialogItem
                        }

                        onProfileChange(profile.copy(nonRootUseDefault = (it.value == Mode.Default)))
                    }
                ) {
                    Icon(painter = painterResource(R.drawable.disabled))
                    Title(R.string.profile)
                    it.title.nullable { t ->
                        Description(t)
                    }
                }

                Crossfade(
                    targetState = (isRootGranted to mode),
                    label = ""
                ) { (granted, currentMode) ->
                    List {
                        if (granted) {
                            if (currentMode == Mode.Custom) {
                                RootProfileConfig(
                                    fixedName = true,
                                    profile = profile,
                                    onProfileChange = onProfileChange
                                )
                            }

                            return@List
                        }

                        val modifyEnabled = currentMode == Mode.Custom

                        SwitchItem(
                            checked = if (modifyEnabled) {
                                profile.umountModules
                            } else {
                                KsuNative.isDefaultUmountModules()
                            },
                            enabled = modifyEnabled,
                            onChange = {
                                onProfileChange(
                                    profile.copy(
                                        umountModules = it,
                                        nonRootUseDefault = false
                                    )
                                )
                            }
                        ) {
                            Title(R.string.settings_umount_modules_default)
                            Description(R.string.settings_umount_modules_default_desc)
                        }
                    }
                }

                Spacer(Modifier.height(bottomBarPaddingValues.calculateBottomPadding()))
            }
        }
    }
}

private enum class Mode(@StringRes private val res: Int) {
    Default(R.string.profile_default),
    Custom(R.string.profile_custom);

    @get:Composable
    val dialogOptions: List<RadioDialogItem<Mode>>
        get() = entries.map {
            RadioDialogItem(
                value = it,
                title = it.text
            )
        }

    @get:Composable
    val text: String get() = stringResource(res)
}