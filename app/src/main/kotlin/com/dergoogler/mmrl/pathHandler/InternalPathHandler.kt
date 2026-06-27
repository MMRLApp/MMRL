package com.dergoogler.mmrl.pathHandler


import android.util.Log
import android.webkit.WebResourceResponse
import androidx.compose.material3.ColorScheme
import com.dergoogler.mmrl.model.WebColors
import dev.mmrlx.webui.PathHandler
import dev.mmrlx.webui.WebUI
import dev.mmrlx.webui.WebUIResourceRequest
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class InternalPathHandler(
    webui: WebUI,
    private val readmeUrl: String,
    private val colorScheme: ColorScheme,
) : PathHandler(webui) {
    override val id = "/internal/"
    val webColors get() = WebColors(colorScheme)
    val assetsPathHandler = AssetsPathHandler(this)

    override fun handle(request: WebUIResourceRequest): WebResourceResponse {
        val path = request.path

        try {
            if (path.matches(Regex("^assets(/.*)?$"))) {
                return assetsPathHandler.handle(
                    WebUIResourceRequest(
                        method = request.method,
                        isForMainFrame = request.isForMainFrame,
                        url = request.url,
                        path = path.removePrefix("assets/"),
                        requestHeaders = request.requestHeaders,
                        isRedirect = request.isRedirect,
                        hasGesture = request.hasGesture()
                    )
                )
            }



            if (path.matches(Regex("readme\\.md"))) {
                val connection = URL(readmeUrl).openConnection() as HttpURLConnection
                connection.connect()
                return htmlResponse(connection.getInputStream())
            }

            if (path.matches(Regex("insets\\.css"))) {
                return insets.css.asStyleResponse()
            }

            if (path.matches(Regex("colors\\.css"))) {
                return webColors.allCssColors.asStyleResponse()
            }

            return notFoundResponse
        } catch (e: IOException) {
            Log.e("InternalPathHandler", "Error opening mmrl asset path: $path", e)
            return notFoundResponse
        }
    }
}