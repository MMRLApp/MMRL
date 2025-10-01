package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.content.NullableBoolean
import com.dergoogler.mmrl.platform.ksu.KernelVersion
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.disableFile
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.removeFile
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback
import com.dergoogler.mmrl.platform.util.Shell.submit

open class KernelSUModuleManager() : BaseModuleManager() {
    override fun getManagerName(): String = "KernelSU"

    override fun getVersion(): String = mVersion

    override fun getVersionCode(): Int {
        val ksuVersion = KsuNative.getVersion()

        return if (ksuVersion != -1) {
            ksuVersion
        } else {
            mVersionCode
        }
    }

    override fun setSuEnabled(enabled: Boolean): Boolean = KsuNative.setSuEnabled(enabled)
    override fun isSuEnabled(): Boolean = KsuNative.isSuEnabled()

    override fun isLkmMode(): NullableBoolean = with(KsuNative) {
        val kernelVersion = KernelVersion.getKernelVersion()
        val ksuVersion = getVersion()

        return NullableBoolean(
            if (ksuVersion >= MINIMAL_SUPPORTED_KERNEL_LKM && kernelVersion.isGKI()) {
                isLkmMode()
            } else {
                null
            }
        )
    }

    override fun getSuperUserCount(): Int = KsuNative.getAllowList().size

    override fun isSafeMode(): Boolean = KsuNative.isSafeMode()

    override fun uidShouldUmount(uid: Int): Boolean = KsuNative.uidShouldUmount(uid)

    override fun getModuleCompatibility() = ModuleCompatibility(
        hasMagicMount = false,
        canRestoreModules = false
    )

    override fun enable(id: ModId, useShell: Boolean, callback: IModuleOpsCallback) {
        val dir = id.moduleDir
        if (!dir.exists()) callback.onFailure(id, null)

        if (useShell) {
            "ksud module enable $id".submit {
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
            "ksud module disable $id".submit {
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
            "ksud module uninstall $id".submit {
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

    override fun getInstallCommand(path: String): String = "ksud module install \"$path\""
    override fun getActionCommand(id: ModId): String = "ksud module action ${id.id}"

    override fun getActionEnvironment(): List<String> = listOf(
        "export ASH_STANDALONE=1",
        "export KSU=true",
        "export KSU_VER=${version}",
        "export KSU_VER_CODE=${versionCode}",
    )
}