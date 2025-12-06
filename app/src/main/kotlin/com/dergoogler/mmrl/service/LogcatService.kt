package com.dergoogler.mmrl.service

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dergoogler.mmrl.utils.log.LogText
import com.dergoogler.mmrl.utils.log.Logcat
import com.dergoogler.mmrl.utils.log.Logcat.toLogTextList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LogcatService : LifecycleService() {
    override fun onCreate() {
        super.onCreate()
        isActive.value = true
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        lifecycleScope.launch(Dispatchers.Default) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Logcat.readLogs().apply {
                    console.addAll(
                        filter { it !in console },
                    )
                }

                while (isActive) {
                    val new =
                        Logcat
                            .getCurrent()
                            .toLogTextList()
                            .filter { it !in console }

                    if (new.isNotEmpty()) {
                        console.addAll(new)
                        Logcat.writeLogs(new)
                    }

                    delay(1000)
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive.value = false
    }

    companion object {
        val console = mutableStateListOf<LogText>()
        val isActive = MutableStateFlow(false)

        fun start(context: Context) {
            val intent = Intent(context, LogcatService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LogcatService::class.java)
            context.stopService(intent)
        }
    }
}
