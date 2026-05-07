package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.ModuleCompatibility
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.disableFile
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.removeFile
import com.dergoogler.mmrl.platform.stub.IModuleOpsCallback
import com.dergoogler.mmrl.platform.util.Shell.submit

open class KsuNextModuleManager : KernelSUModuleManager() {
    override fun getModuleCompatibility() =
        ModuleCompatibility(
            hasMagicMount = false,
            canRestoreModules = true,
        )

    override fun enable(
        id: ModId,
        useShell: Boolean,
        callback: IModuleOpsCallback,
    ) {
        val dir = id.moduleDir
        if (!dir.exists()) callback.onFailure(id, null)

        if (useShell) {
            "ksud module restore $id && ksud module enable $id".submit {
                if (isSuccess) {
                    callback.onSuccess(id)
                } else {
                    callback.onFailure(id, out.joinToString())
                }
            }
        } else {
            runCatching {
                id.removeFile.apply { if (exists()) delete() }
                id.disableFile.apply { if (exists()) delete() }
            }.onSuccess {
                callback.onSuccess(id)
            }.onFailure {
                callback.onFailure(id, it.message)
            }
        }
    }

    override fun getActionEnvironment(): List<String> =
        listOf(
            "export ASH_STANDALONE=1",
            "export KSU=true",
            "export KSU_NEXT=true",
            "export KSU_VER=$version",
            "export KSU_VER_CODE=$versionCode",
        )
}
