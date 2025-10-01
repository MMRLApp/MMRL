package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.content.NullableBoolean
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.disableFile
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.removeFile
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback
import com.dergoogler.mmrl.platform.util.Shell.submit

open class APatchModuleManager() : BaseModuleManager() {
    override fun getManagerName(): String = "APatch"

    override fun getVersion(): String = mVersion

    override fun getVersionCode(): Int = mVersionCode

    override fun getModuleCompatibility() = ModuleCompatibility(
        hasMagicMount = SuFile("/data/adb/.bind_mount_enable").exists() && (versionCode >= 11011 && !SuFile(
            "/data/adb/.overlay_enable"
        ).exists()),
        canRestoreModules = false
    )

    override fun enable(id: ModId, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = id.moduleDir
        if (!dir.exists()) callback.onFailure(id, null)

        if (useShell) {
            "apd module enable $id".submit {
                if (isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, out.joinToString())
                }
            }
        } else {
            runCatching {
                dir.resolve("remove").apply { if (exists()) delete() }
                dir.resolve("disable").apply { if (exists()) delete() }
            }.onSuccess {
                callback.onSuccess(id)
            }.onFailure {
                callback.onFailure(id, it.message)
            }
        }
    }

    override fun disable(id: ModId, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = id.moduleDir
        if (!dir.exists()) return callback.onFailure(id, null)

        if (useShell) {
            "apd module disable $id".submit {
                if (isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, out.joinToString())
                }
            }
        } else {
            runCatching {
                id.removeFile.apply { if (exists()) delete() }
                id.disableFile.createNewFile()
            }.onSuccess {
                callback.onSuccess(id)
            }.onFailure {
                callback.onFailure(id, it.message)
            }
        }
    }

    override fun remove(id: ModId, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = id.moduleDir
        if (!dir.exists()) return callback.onFailure(id, null)

        if (useShell) {
            "apd module uninstall $id".submit {
                if (isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, out.joinToString())
                }
            }
        } else {
            runCatching {
                id.disableFile.apply { if (exists()) delete() }
                id.removeFile.createNewFile()
            }.onSuccess {
                callback.onSuccess(id)
            }.onFailure {
                callback.onFailure(id, it.message)
            }
        }
    }

    override fun getInstallCommand(path: String): String = "apd module install \"$path\""
    override fun getActionCommand(id: ModId): String = "apd module action ${id.id}"

    override fun getActionEnvironment(): List<String> = listOf(
        "export ASH_STANDALONE=1",
        "export APATCH=true",
        "export APATCH_VER=${version}",
        "export APATCH_VER_CODE=${versionCode}",
    )

    // KernelSU only
    override fun isSafeMode(): Boolean = false
    override fun isLkmMode(): NullableBoolean = NullableBoolean(null)
    override fun setSuEnabled(enabled: Boolean): Boolean = true
    override fun isSuEnabled(): Boolean = true
    override fun getSuperUserCount(): Int = -1
    override fun uidShouldUmount(uid: Int): Boolean = false
}