package com.dergoogler.mmrl.ui.activity

import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import com.dergoogler.mmrl.ext.nullply
import com.dergoogler.mmrl.ext.tmpDir
import com.dergoogler.mmrl.viewmodel.TerminalViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlin.getValue
import kotlin.reflect.KClass

abstract class TerminalActivity : MMRLComponentActivity() {

    protected open var terminalJob: Job? = null
    override val windowFlags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

    private fun TerminalViewModel.cancelJob(message: String) {
        try {
            terminalJob?.cancel(message)
            terminal.shell.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel job", e)
        }
    }

    protected fun TerminalViewModel.destroy() {
        Log.d(TAG, "$TAG destroy")
        tmpDir.deleteRecursively()
        cancelJob("$TAG was destroyed")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "TerminalActivity"
    }
}