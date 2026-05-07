package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.ModuleCompatibility

open class SukiSUModuleManager : KernelSUModuleManager() {
    override fun getManagerName(): String = "SukiSU"

    override fun getModuleCompatibility() =
        ModuleCompatibility(
            hasMagicMount = true,
            canRestoreModules = false,
        )

    override fun getActionEnvironment(): List<String> =
        listOf(
            "export ASH_STANDALONE=1",
            "export KSU=true",
            "export KSU_SUKISU=true",
            "export KSU_VER=$version",
            "export KSU_VER_CODE=$versionCode",
        )
}
