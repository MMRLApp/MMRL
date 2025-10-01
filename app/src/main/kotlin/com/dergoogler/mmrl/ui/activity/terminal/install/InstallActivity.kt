package com.dergoogler.mmrl.ui.activity.terminal.install

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.nullply
import com.dergoogler.mmrl.ext.nullvoke
import com.dergoogler.mmrl.ext.tmpDir
import com.dergoogler.mmrl.ui.component.dialog.ConfirmDialog
import com.dergoogler.mmrl.viewmodel.InstallViewModel
import dev.dergoogler.mmrl.compat.BuildCompat
import com.dergoogler.mmrl.ui.activity.MMRLComponentActivity
import com.dergoogler.mmrl.ui.activity.TerminalActivity
import com.dergoogler.mmrl.ui.activity.setBaseContent
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

class InstallActivity : TerminalActivity() {
    private var confirmDialog by mutableStateOf(true)
    private val viewModel by viewModels<InstallViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("InstallActivity onCreate")
        super.onCreate(savedInstanceState)

        val uris: ArrayList<Uri>? = if (intent.data != null) {
            arrayListOf(intent.data!!)
        } else {
            if (BuildCompat.atLeastT) {
                intent.getParcelableArrayListExtra("uris", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("uris")
            }
        }

        Log.d(TAG, "InstallActivity onCreate: $uris")

        val confirm = intent.getBooleanExtra("confirm", true)

        if (uris.isNullOrEmpty()) {
            finish()
            return
        }

        if (!confirm) {
            initModule(uris.toList())
        }

        setBaseContent {
            if (confirm && confirmDialog) ConfirmDialog(
                title = R.string.install_screen_confirm_title,
                description = R.string.install_screen_confirm_text,
                onClose = {
                    confirmDialog = false
                    viewModel.terminal.shell.close()
                    finish()
                },
                onConfirm = {
                    confirmDialog = false
                    initModule(uris.toList())
                }
            )

            InstallScreen(viewModel)
        }
    }

    override fun onDestroy() {
        Timber.d("InstallActivity onDestroy")
        tmpDir.deleteRecursively()
        viewModel.destroy()
        super.onDestroy()
    }

    private fun initModule(uris: List<Uri>) {
        val job = lifecycleScope.launch {
            viewModel.installModules(
                uris = uris
            )
        }

        terminalJob = job
    }

    companion object {
        private const val TAG = "InstallActivity"

        fun start(context: Context, uri: List<Uri>, confirm: Boolean = true) {
            val intent = Intent(context, InstallActivity::class.java)
                .apply {
                    putExtra("confirm", confirm)
                    putParcelableArrayListExtra("uris", ArrayList(uri))
                }

            context.startActivity(intent)
        }

        fun start(context: Context, uri: Uri, confirm: Boolean = true) {
            start(context, listOf(uri), confirm)
        }
    }
}