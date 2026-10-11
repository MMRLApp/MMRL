package com.dergoogler.mmrl.ui.screens.moduleView

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.model.WebColors
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurToolbar
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.MimeUtil
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.chrisbanes.haze.hazeSource
import dev.mmrlx.compose.webui.WebUIView
import dev.mmrlx.compose.webui.insets
import dev.mmrlx.compose.webui.rememberWebUIState
import dev.mmrlx.webui.RouteRegistry
import dev.mmrlx.webui.RouteScope
import dev.mmrlx.webui.WebResourceResponse
import dev.mmrlx.webui.WebUIOptions
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
@Destination<RootGraph>
fun ViewDescriptionScreen(readmeUrl: String) =
    LocalScreenProvider {
        val navigator = LocalDestinationsNavigator.current
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val userPrefs = LocalUserPreferences.current
        val scheme = MaterialTheme.colorScheme

        val wstate =
            rememberWebUIState(
                "https://desc.mmrl.dev",
                WebUIOptions("internal/assets/markdown.html")
            ) {
                it
                    .settings {
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
                    .registerRoutes {
                        val webColors = WebColors(scheme)

                        internalRoute("readme.md") {
                            val connection = URL(readmeUrl).openConnection() as HttpURLConnection
                            connection.connect()
                            htmlResponse(connection.getInputStream())
                        }

                        internalRoute("insets.css") {
                            insets.css.asStyleResponse()
                        }

                        internalRoute("colors.css") {
                            webColors.allCssColors.asStyleResponse()
                        }

                        internalRoute("assets/*asset") {
                            val filePath =
                                request.params["asset"] ?: return@internalRoute notFoundResponse

                            try {
                                val inputStream = kontext.assets.open(filePath)
                                val mimeType = MimeUtil.getMimeFromFileName(filePath)
                                response(mimeType = mimeType, data = inputStream)
                            } catch (e: Exception) {
                                console.error("Failed to open $filePath from the assets: $e")
                                notFoundResponse
                            }
                        }
                    }
            }

        DisposableEffect(Unit) {
            onDispose {
                wstate.destroy()
            }
        }

        // We have to override otherwise the WebUI will close the entire app
        BackHandler {
            if (wstate.canGoBack()) {
                wstate.goBack()
                return@BackHandler
            }

            navigator.popBackStack()
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
            wstate.insets(
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

private fun RouteRegistry.internalRoute(
    name: String,
    content: RouteScope.() -> WebResourceResponse,
) = route("internal/$name", content)