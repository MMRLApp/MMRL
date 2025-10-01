package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.content.NullableBoolean
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.disableFile
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.removeFile
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback

open class MagiskModuleManager() : BaseModuleManager() {
    override fun getManagerName(): String = "Magisk"

    override fun getModuleCompatibility() = ModuleCompatibility(
        hasMagicMount = true,
        canRestoreModules = true
    )

    override fun getVersion(): String = mVersion

    override fun getVersionCode(): Int = mVersionCode


    override fun enable(id: ModId, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = id.moduleDir
        if (!dir.exists()) callback.onFailure(id, null)

        runCatching {
            id.removeFile.apply { if (exists()) delete() }
            id.disableFile.apply { if (exists()) delete() }
        }.onSuccess {
            callback.onSuccess(id)
        }.onFailure {
            callback.onFailure(id, it.message)
        }
    }

    override fun disable(id: ModId, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = id.moduleDir
        if (!dir.exists()) return callback.onFailure(id, null)

        runCatching {
            id.removeFile.apply { if (exists()) delete() }
            id.disableFile.createNewFile()
        }.onSuccess {
            callback.onSuccess(id)
        }.onFailure {
            callback.onFailure(id, it.message)
        }
    }

    override fun remove(id: ModId, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = id.moduleDir
        if (!dir.exists()) return callback.onFailure(id, null)

        runCatching {
            id.disableFile.apply { if (exists()) delete() }
            id.removeFile.createNewFile()
        }.onSuccess {
            callback.onSuccess(id)
        }.onFailure {
            callback.onFailure(id, it.message)
        }
    }

    override fun getInstallCommand(path: String): String = "magisk --install-module \"$path\""
    override fun getActionCommand(id: ModId): String = ""

    override fun getActionEnvironment(): List<String> = listOf(
        "export ASH_STANDALONE=1",
        "export MAGISK=true",
        "export MAGISK_VER=${version}",
        "export MAGISKTMP=$(magisk --path)",
        "export MAGISK_VER_CODE=${versionCode}",
    )

    // KernelSU only
    override fun isSafeMode(): Boolean = false
    override fun isLkmMode(): NullableBoolean = NullableBoolean(null)
    override fun setSuEnabled(enabled: Boolean): Boolean = true
    override fun isSuEnabled(): Boolean = true
    override fun getSuperUserCount(): Int = -1
    override fun uidShouldUmount(uid: Int): Boolean = false
}