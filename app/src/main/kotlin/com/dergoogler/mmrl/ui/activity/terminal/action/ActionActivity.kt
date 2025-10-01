package com.dergoogler.mmrl.ui.activity.terminal.action

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.lifecycleScope
import com.dergoogler.mmrl.ext.tmpDir
import com.dergoogler.mmrl.platform.content.LocalModule
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.getModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.isNullOrEmpty
import com.dergoogler.mmrl.platform.model.ModId.Companion.putModId
import com.dergoogler.mmrl.viewmodel.ActionViewModel
import com.dergoogler.mmrl.ui.activity.MMRLComponentActivity
import com.dergoogler.mmrl.ui.activity.TerminalActivity
import com.dergoogler.mmrl.ui.activity.setBaseContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class ActionActivity : TerminalActivity() {
    private val viewModel by viewModels<ActionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        val modId = intent.getModId()

        if (modId.isNullOrEmpty()) {
            finish()
        } else {
            Log.d(TAG, "onCreate: $modId")
            initAction(modId)
        }

        setBaseContent {
            ActionScreen(viewModel)
        }
    }

    private fun initAction(modId: ModId) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.runAction(modId)
        }
    }

    override fun onDestroy() {
        viewModel.destroy()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ActionActivity"

        fun start(context: Context, modId: ModId) {
            val intent = Intent(context, ActionActivity::class.java)
                .apply {
                    putModId(modId)
                }

            context.startActivity(intent)
        }
    }
}