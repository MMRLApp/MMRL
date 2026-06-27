package com.dergoogler.mmrl.ui.screens.moduleView

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.pathHandler.InternalPathHandler
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurToolbar
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.chrisbanes.haze.hazeSource
import dev.mmrlx.compose.webui.WebUIView
import dev.mmrlx.compose.webui.insets
import dev.mmrlx.compose.webui.rememberWebUIState

const val launchUrl = "https://mui.kernelsu.org/internal/assets/markdown.html"

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
@Destination<RootGraph>
fun ViewDescriptionScreen(readmeUrl: String) =
    LocalScreenProvider {
        val density = LocalDensity.current
        val navigator = LocalDestinationsNavigator.current
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val userPrefs = LocalUserPreferences.current
        val scheme = MaterialTheme.colorScheme

        @Suppress("KotlinConstantConditions") val wstate =
            rememberWebUIState("https://desc.mmrl.dev", "internal/assets/markdown.html") {
                it
                    .settings {
                        schemeWhitelist += "ksu"
                        useDefaultApplicationInterface = false
                        useDefaultFileSystem = false
                        debug = BuildConfig.DEBUG
                        forceKillProcess = false
                        useConsoleInterceptor = false
                        darkMode = userPrefs.isDarkMode()
                    }
                    .client {
                        // not related to client
                        it.webview.setBackgroundColor(scheme.background.toArgb())
                    }
                    .chromeClient { }
                    .registerPathHandler(
                        InternalPathHandler::class.java
                    ) {
                        add(
                            String::class.java to readmeUrl
                        )
                        add(
                            ColorScheme::class.java to scheme
                        )
                    }
            }

        DisposableEffect(Unit) {
            onDispose {
                wstate.webui.destroy()
            }
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopBar(
                    scrollBehavior = scrollBehavior,
                    navigator = navigator,
                )
            },
            contentWindowInsets = WindowInsets.none,
        ) { innerPadding ->
            val bottomBarPaddingValues = LocalMainScreenInnerPaddings.current
            wstate.webui.insets(
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = bottomBarPaddingValues.calculateBottomPadding(),
                )
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .hazeSource(LocalHazeState.current),
            ) {
                this@Scaffold.ResponsiveContent {
                    WebUIView(wstate)
                }
            }
        }
    }

@Composable
private fun TopBar(
    navigator: DestinationsNavigator,
    scrollBehavior: TopAppBarScrollBehavior,
) = BlurToolbar(
    navigationIcon = {
        IconButton(onClick = { navigator.popBackStack() }) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = null,
            )
        }
    },
    title = { Text(text = stringResource(id = R.string.view_module_about_this_module)) },
    scrollBehavior = scrollBehavior,
)