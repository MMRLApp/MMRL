package com.dergoogler.mmrl.compat

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.util.UUID

object PermissionCompat {
    data class PermissionState(
        private val results: Map<String, Boolean>
    ) {
        val allGranted = results.all { it.value }

        override fun toString(): String {
            return results.toString()
        }
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }

        return null
    }

    fun checkPermissions(
        context: Context,
        permissions: List<String>
    ): PermissionState {
        val results = permissions.associateWith {
            ContextCompat.checkSelfPermission(
                context, it
            ) == PackageManager.PERMISSION_GRANTED
        }

        return PermissionState(results)
    }

    fun requestPermissions(
        context: Context,
        permissions: List<String>,
        callback: (PermissionState) -> Unit
    ) {
        val state = checkPermissions(context, permissions)
        if (state.allGranted) {
            callback(state)
            return
        }

        val activity = context.findActivity()
        if (activity !is ActivityResultRegistryOwner) return

        val activityResultRegistry = activity.activityResultRegistry
        val key = UUID.randomUUID().toString()
        val launcher = activityResultRegistry.register(
            key,
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            callback(PermissionState(results))
        }

        launcher.launch(permissions.toTypedArray())
    }
}