package com.dergoogler.mmrl.ui.screens.home

import android.os.Build
import android.system.Os
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.WorkingMode.Companion.isNonRoot
import com.dergoogler.mmrl.datastore.model.WorkingMode.Companion.isRoot
import com.dergoogler.mmrl.ext.managerVersion
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.model.online.Changelog
import com.dergoogler.mmrl.network.runRequest
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toFormattedFileSize
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.stub.IMMRLApiManager
import com.dergoogler.mmrl.ui.component.Alert
import com.dergoogler.mmrl.ui.component.BottomBar
import com.dergoogler.mmrl.ui.component.SELinuxStatus
import com.dergoogler.mmrl.ui.component.TopAppBarEventIcon
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.ui.remember.rememberLocalAnalytics
import com.dergoogler.mmrl.ui.remember.seLinuxContext
import com.dergoogler.mmrl.ui.remember.superUserCount
import com.dergoogler.mmrl.ui.remember.versionName
import com.dergoogler.mmrl.ui.screens.home.components.NonRootItem
import com.dergoogler.mmrl.ui.screens.home.components.RebootBottomSheet
import com.dergoogler.mmrl.ui.screens.home.components.RootItem
import com.dergoogler.mmrl.ui.screens.settings.changelogs.items.ChangelogBottomSheet
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AboutScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ThankYouScreenDestination
import dev.mmrlx.compose.layout.outlinedCard
import dev.mmrlx.compose.ui.LinearProgressIndicator
import dev.mmrlx.compose.ui.LocalTextStyle
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.icon.IconButton
import dev.mmrlx.compose.ui.list.List
import dev.mmrlx.compose.ui.list.component.Item
import dev.mmrlx.compose.ui.list.component.item.Description
import dev.mmrlx.compose.ui.list.component.item.Icon
import dev.mmrlx.compose.ui.list.component.item.Title
import dev.mmrlx.compose.ui.scaffold.Scaffold
import dev.mmrlx.compose.ui.toolbar.Toolbar
import dev.mmrlx.compose.ui.toolbar.ToolbarDefaults
import dev.mmrlx.compose.ui.toolbar.ToolbarScrollBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

val listItemContentPaddingValues: PaddingValues = PaddingValues(vertical = 8.dp, horizontal = 25.dp)

@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen() {
    val navigator = LocalDestinationsNavigator.current
    val scrollBehavior = ToolbarDefaults.fadePinnedScrollBehavior()
    val context = LocalContext.current
    val userPreferences = LocalUserPreferences.current
    val browser = LocalUriHandler.current

    val analytics by rememberLocalAnalytics()

    var openRebootSheet by remember { mutableStateOf(false) }
    if (openRebootSheet) {
        RebootBottomSheet(
            onClose = { openRebootSheet = false },
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        toolbar = {
            TopBar0(
                onInfoClick = {
                    navigator.navigate(AboutScreenDestination)
                },
                onHeartClick = {
                    navigator.navigate(ThankYouScreenDestination)
                },
                onRebootClick = {
                    openRebootSheet = true
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomBar()
        },
        contentWindowInsets = WindowInsets.none,
    ) {
        Column(
            modifier =
                Modifier
                    .scaffoldHazeSource()
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .scaffoldPadding()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                userPreferences.workingMode.isRoot ->
                    RootItem()

                userPreferences.workingMode.isNonRoot ->
                    NonRootItem()
            }

            if (userPreferences.checkAppUpdates) {
                var changelog by remember { mutableStateOf<List<Changelog>?>(null) }
                LaunchedEffect(Unit) {
                    runRequest {
                        withContext(Dispatchers.IO) {
                            val api = IMMRLApiManager.build()
                            return@withContext api.changelog.execute()
                        }
                    }.onSuccess { list ->
                        changelog = list
                    }.onFailure {
                        Timber.e(it, "unable to get changelog")
                    }
                }

                changelog?.let {
                    val latest = it.first()

                    var changelogSheet by remember { mutableStateOf(false) }
                    if (changelogSheet) {
                        ChangelogBottomSheet(
                            changelog = latest,
                            onClose = { changelogSheet = false },
                        )
                    }

                    AnimatedVisibility(
                        visible =
                            userPreferences.developerMode { devAlwaysShowUpdateAlert } ||
                                    (
                                            if (latest.preRelease) {
                                                userPreferences.checkAppUpdatesPreReleases &&
                                                        latest.versionCode > context.managerVersion.second
                                            } else {
                                                latest.versionCode > context.managerVersion.second
                                            }
                                            ),
                        enter = fadeIn() + expandVertically(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        Alert(
                            onClick = {
                                changelogSheet = true
                            },
                            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                            title = stringResource(R.string.update_available),
                            message =
                                stringResource(
                                    R.string.new_version_available,
                                    latest.versionName,
                                ),
                            icon = R.drawable.cloud_download,
                        )
                    }
                }
            }

            List(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = listItemContentPaddingValues,
            ) {
                val scope = this

                Column(
                    modifier = Modifier
                        .outlinedCard()
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                ) {
                    val uname = Os.uname()

                    scope.Item {
                        Title {
                            Text(
                                "DEV BUILD! EXPECT ERRORS AND BROKEN UI/UX",
                                style = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Red
                                )
                            )
                        }
                    }

                    scope.Item {
                        Icon(painter = painterResource(R.drawable.cookie_man))
                        Title(R.string.kernel)
                        Description(uname.release)
                    }

                    scope.Item {
                        Icon(painter = painterResource(R.drawable.launcher_outline))
                        Title(R.string.manager_version)
                        Description("${context.managerVersion.first} (${context.managerVersion.second})")
                    }

                    scope.Item {
                        Icon(painter = painterResource(R.drawable.fingerprint))
                        Title(R.string.fingerprint)
                        Description(
                            if (userPreferences.hideFingerprintInHome) {
                                stringResource(id = R.string.hidden)
                            } else {
                                Build.FINGERPRINT
                            },
                        )
                    }

                    scope.Item {
                        Icon(painter = painterResource(R.drawable.cpu_2))
                        Title(R.string.architecture)
                        Description(uname.machine)
                    }

                    scope.SELinuxStatus()

                    PlatformManager.platform.isKernelSuOrNext.takeTrue {
                        scope.Item {
                            Icon(painter = painterResource(R.drawable.user_outlined))
                            Title(R.string.super_user_apps)
                            Description(superUserCount.toString())
                        }
                    }
                }

                PlatformManager.isAlive.takeTrue {
                    userPreferences.developerMode.takeTrue {
                        Column(
                            modifier = Modifier
                                .outlinedCard()
                                .padding(vertical = 16.dp)
                                .fillMaxWidth()
                        ) {
                            scope.Item {
                                Title(R.string.home_root_provider_version_name)
                                Description(versionName)
                            }

                            scope.Item {
                                Title(R.string.home_root_provider_se_linux_context)
                                Description(seLinuxContext)
                            }

                            if (PlatformManager.platform.isKernelSuNext) {
                                KsuNative.getHookMode().nullable {
                                    scope.Item {
                                        Title(R.string.hook_mode)
                                        Description(it)
                                    }
                                }
                            }

                            if (PlatformManager.platform.isSukiSU) {
                                KsuNative.getHookType().nullable {
                                    scope.Item {
                                        Title(R.string.hook_mode)
                                        Description(it)
                                    }
                                }

                                scope.Item {
                                    Title(R.string.kpm_support)
                                    Description(if (KsuNative.isKPMEnabled()) R.string.yes else R.string.no)
                                }
                            }
                        }
                    }
                }

                if (PlatformManager.platform.isValid) {
                    analytics.nullable {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .outlinedCard()
                                    .padding(vertical = 16.dp)
                                    .weight(1f)
                            ) {
                                scope.Item {
                                    Title(R.string.home_installed_modules)
                                    Description(it.totalModules.toString())
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .outlinedCard()
                                    .padding(vertical = 16.dp)
                                    .weight(1f)
                            ) {
                                scope.Item {
                                    Title(R.string.home_updated_modules)
                                    Description(it.totalUpdated.toString())
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .outlinedCard()
                                .padding(vertical = 16.dp)
                                .fillMaxWidth()
                        ) {
                            scope.Item {
                                Title(R.string.home_storage_usage)

                                Description {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = it.totalModulesUsageBytes.toFormattedFileSize(),
                                        )

                                        LinearProgressIndicator(
                                            progress = {
                                                it.totalStorageUsage
                                            },
                                            modifier =
                                                Modifier
                                                    .height(10.dp)
                                                    .weight(1f),
                                            drawStopIndicator = {},
                                        )

                                        Text(
                                            text = it.totalDeviceStorageBytes.toFormattedFileSize(),
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .outlinedCard()
                                    .padding(vertical = 16.dp)
                                    .weight(1f)
                            ) {
                                scope.Item {
                                    Title(R.string.home_enabled_modules)
                                    Description(it.totalEnabled.toString())
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .outlinedCard()
                                    .padding(vertical = 16.dp)
                                    .weight(1f)
                            ) {
                                scope.Item {
                                    Title(R.string.home_disabled_modules)
                                    Description(it.totalDisabled.toString())
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .outlinedCard()
                        .fillMaxWidth()
                        .clickable {
                            browser.openUri("https://github.com/sponsors/MMRLApp")
                        }
                        .padding(vertical = 16.dp)
                ) {
                    scope.Item {
                        Title(
                            id = R.string.home_support_title,
                            styleTransform = {
                                val newStyle = it.copy(color = Color.Unspecified)
                                it.merge(newStyle)
                            },
                        )

                        Description(
                            id = R.string.home_support_content,
                            styleTransform = {
                                val newStyle = it.copy(color = Color.Unspecified)
                                it.merge(newStyle)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar0(
    onRebootClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onHeartClick: () -> Unit = {},
    scrollBehavior: ToolbarScrollBehavior,
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            TopAppBarEventIcon()
        },
        actions = {
            if (PlatformManager.isAlive) {
                IconButton(onClick = onRebootClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.refresh),
                        contentDescription = null,
                    )
                }
            }

            IconButton(onClick = onHeartClick) {
                Icon(
                    painter = painterResource(id = R.drawable.heart),
                    contentDescription = null,
                )
            }

            IconButton(onClick = onInfoClick) {
                Icon(
                    painter = painterResource(id = R.drawable.info_circle),
                    contentDescription = null,
                )
            }
        },
    )
}
