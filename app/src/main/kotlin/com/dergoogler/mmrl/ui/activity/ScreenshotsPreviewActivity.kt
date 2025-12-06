package com.dergoogler.mmrl.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.dergoogler.mmrl.ext.tmpDir
import timber.log.Timber

class ScreenshotsPreviewActivity : MMRLComponentActivity() {
    override val windowFlags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("ScreenshotsPreviewActivity onCreate")
        super.onCreate(savedInstanceState)

        val urls: ArrayList<String>? = intent.getStringArrayListExtra("urls")
        val index: Int = intent.getIntExtra("index", 0)

        if (urls.isNullOrEmpty()) {
            finish()
            return
        }

        setBaseContent {
            ScreenshotsPreviewScreen(index, urls)
        }
    }

    override fun onDestroy() {
        Timber.d("InstallActivity onDestroy")
        tmpDir.deleteRecursively()
        super.onDestroy()
    }

    companion object {
        fun start(
            context: Context,
            url: List<String>,
            index: Int,
        ) {
            val intent =
                Intent(context, ScreenshotsPreviewActivity::class.java)
                    .apply {
                        putExtra("index", index)
                        putStringArrayListExtra("urls", ArrayList(url))
                    }

            context.startActivity(intent)
        }

        fun start(
            context: Context,
            url: String,
            index: Int,
        ) {
            start(context, listOf(url), index)
        }
    }
}
