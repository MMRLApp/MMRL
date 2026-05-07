package com.dergoogler.mmrl.pathHandler

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.WebResourceResponse
import com.dergoogler.mmrl.hybridwebui.HybridWebUI
import com.dergoogler.mmrl.hybridwebui.HybridWebUIResourceRequest
import java.io.IOException

class AssetsPathHandler(
    private val context: Context
) : HybridWebUI.PathHandler() {

    private val assetHelper get() = context.assets

    @SuppressLint("WrongThread")
    override fun handle(
        view: HybridWebUI,
        request: HybridWebUIResourceRequest,
    ): WebResourceResponse {
        val path = request.path

        try {
            val inputStream = assetHelper.open(path.removePrefix("/"))
            val mimeType = HybridWebUI.MimeType.getMimeFromFileName(path)
            return WebResourceResponse(mimeType, null, inputStream)
        } catch (e: IOException) {
            Log.e("assetsPathHandler", "Error opening asset path: $path", e)
            return notFoundResponse
        }
    }
}
