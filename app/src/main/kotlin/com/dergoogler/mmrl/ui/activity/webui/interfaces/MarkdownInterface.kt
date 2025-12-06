package com.dergoogler.mmrl.ui.activity.webui.interfaces

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.model.JavaScriptInterface

class MarkdownInterface(
    wxOptions: WXOptions,
    private val readme: String,
) : WXInterface(wxOptions) {
    override var name: String = "markdown"

    companion object {
        fun factory(readme: String) =
            JavaScriptInterface(
                clazz = MarkdownInterface::class.java,
                initargs = arrayOf(readme),
                parameterTypes = arrayOf(String::class.java),
            )
    }

    @JavascriptInterface
    fun get() = readme
}
