package com.dergoogler.mmrl.ui.activity

import android.os.Bundle

class CrashHandlerActivity : MMRLComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val message = intent.getStringExtra("message") ?: "Unknown Message"
        val stacktrace = intent.getStringExtra("stacktrace") ?: "Unknown Stacktrace"
        val helpMessage = intent.getStringExtra("helpMessage")

        setBaseContent {
            CrashHandlerScreen(
                message = message,
                stacktrace = stacktrace,
                helpMessage = helpMessage,
            )
        }
    }
}
