package com.dergoogler.mmrl.model.online

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.ext.isNotNullOrEmpty
import com.dergoogler.mmrl.platform.Platform
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class ModuleManager(
    val magisk: ModuleManagerSolution? = null,
    val kernelsu: ModuleManagerSolution? = null,
    val ksunext: ModuleManagerSolution? = null,
    val apatch: ModuleManagerSolution? = null,
) {
    operator fun get(platform: Platform) = with(platform) {
        when {
            isMagisk -> magisk
            isKernelSU -> kernelsu
            isKernelSuNext -> ksunext
            isAPatch -> apatch
            else -> ModuleManagerSolution()
        }
    }

    val all: List<ModuleManagerSolution>
        get() = listOfNotNull(
            magisk,
            kernelsu,
            ksunext,
            apatch,
        )
}

@Serializable
@JsonClass(generateAdapter = true)
data class ModuleManagerSolution(
    val min: Int? = null,
    val devices: List<String>? = null,
    val arch: List<String>? = null,
    val require: List<String>? = null,
) {
    @SuppressLint("ComposableNaming")
    @Composable
    fun isNotSupportedRootVersion(
        version: Int,
        block: @Composable (Int) -> Unit,
    ) {
        if (isNotSupportedRootVersion(version)) {
            block(min!!)
        }
    }

    private fun isNotSupportedRootVersion(
        version: Int,
    ) = min != null && version < min || min == -1

    private fun isNotSupportedDevice() =
        devices.isNotNullOrEmpty() && !devices.map { it.lowercase() }
            .contains(Build.MODEL.lowercase())

    @SuppressLint("ComposableNaming")
    @Composable
    fun isNotSupportedDevice(
        block: @Composable () -> Unit,
    ) {
        if (isNotSupportedDevice()) {
            block()
        }
    }

    private fun isNotSupportedArch() = arch.isNotNullOrEmpty() && !arch.map { it.lowercase() }
        .contains(Build.SUPPORTED_ABIS[0].lowercase())

    @SuppressLint("ComposableNaming")
    @Composable
    fun isNotSupportedArch(
        block: @Composable () -> Unit,
    ) {
        if (isNotSupportedArch()) {
            block()
        }
    }
}