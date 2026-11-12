package com.dergoogler.mmrl.ui.activity.terminal.install

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.activity.TerminalActivity
import com.dergoogler.mmrl.ui.activity.setBaseContent
import com.dergoogler.mmrl.ui.component.dialog.ConfirmDialog
import com.dergoogler.mmrl.viewmodel.InstallViewModel
import dev.dergoogler.mmrl.compat.BuildCompat
import kotlinx.coroutines.launch
import timber.log.Timber

class InstallActivity : TerminalActivity() {
    private var confirmDialog by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("InstallActivity onCreate")
        super.onCreate(savedInstanceState)

        val uris: ArrayList<Uri>? =
            if (intent.data != null) {
                arrayListOf(intent.data!!)
            } else {
                if (BuildCompat.atLeastT) {
                    intent.getParcelableArrayListExtra("uris", Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra("uris")
                }
            }

        val confirm = intent.getBooleanExtra("confirm", true)

        if (uris.isNullOrEmpty()) {
            finish()
            return
        }

        setBaseContent {
            InstallViewModel.LocalProvider {
                if (!confirm) {
                    it.initModule(uris.toList())
                }

                if (confirm && confirmDialog) {
                    ConfirmDialog(
                        title = R.string.install_screen_confirm_title,
                        description = R.string.install_screen_confirm_text,
                        onClose = {
                            confirmDialog = false
                            finish()
                        },
                        onConfirm = {
                            confirmDialog = false
                            it.initModule(uris.toList())
                        },
                    )
                }

                InstallScreen()
            }
        }
    }

    private fun InstallViewModel.initModule(uris: List<Uri>) {
        val job =
            lifecycleScope.launch {
                installModules(
                    uris = uris,
                )
            }

        terminalJob = job
    }

    companion object {
        private const val TAG = "InstallActivity"

        fun start(
            context: Context,
            uri: List<Uri>,
            confirm: Boolean = true,
        ) {
            val intent =
                Intent(context, InstallActivity::class.java)
                    .apply {
                        putExtra("confirm", confirm)
                        putParcelableArrayListExtra("uris", ArrayList(uri))
                    }

            context.startActivity(intent)
        }

        fun start(
            context: Context,
            uri: Uri,
            confirm: Boolean = true,
        ) {
            start(context, listOf(uri), confirm)
        }
    }
}
