package com.dergoogler.mmrl.ui.activity.terminal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.snapshots.SnapshotStateList

class ShellBroadcastReceiver(
    private val context: Context,
    private val console: SnapshotStateList<String>,
    private val logs: MutableList<String>,
) : BroadcastReceiver() {
    override fun onReceive(
        ctx: Context?,
        intent: Intent?,
    ) {
        if (intent == null) return

        when {
            action(Actions.SET_LAST_LINE, intent) -> {
                val text = intent.getStringExtra("text")
                text?.let {
                    console[console.lastIndex] = it
                }
            }

            action(Actions.REMOVE_LAST_LINE, intent) -> {
                console.removeAt(console.lastIndex)
            }

            action(Actions.CLEAR_TERMINAL, intent) -> {
                console.clear()
            }

            action(Actions.LOG, intent) -> {
                val text = intent.getStringExtra("text")
                text?.let {
                    console.add(it)
                    logs.add(it)
                }
            }
        }
    }

    private fun action(
        action: Actions,
        intent: Intent?,
    ) = intent != null && intent.action == "${context.packageName}.${action.name}"
}
