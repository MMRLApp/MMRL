package com.dergoogler.mmrl.manager

import com.dergoogler.mmrl.Platform
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.repository.LocalRepository
import com.topjohnwu.superuser.Shell
import dev.mmrlx.nio.SuFile
import org.apache.commons.compress.archivers.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class RootManagerRepository @Inject constructor(
    private val localRepository: LocalRepository,
) {
    var manager = RootManager()
    val platform get() = manager.platform
    val modules get() = manager.modules

    fun findModuleById(id: String): LocalModule? = modules.find { it.id == id }

    fun reboot(reason: String) {
        if (reason == "recovery") {
            Shell.cmd("/system/bin/input keyevent 26").exec()
            return
        }

        Shell.cmd("/system/bin/svc power reboot $reason || /system/bin/reboot $reason").exec()
    }

    fun getModuleInfoFromZip(path: String): LocalModule? = getModuleInfoFromZip(SuFile(path))
    fun getModuleInfoFromZip(zipFile: SuFile): LocalModule? {
        val rawZipFile = ZipFile.Builder().setInputStream(zipFile.inputStream()).get()
        val entry = rawZipFile.getEntry(ModId.PROP_FILE) ?: return null
        val stream = rawZipFile.getInputStream(entry)
        return LocalModule("", propsSource = stream)
    }
}

open class RootManager() {
    open val platform: Platform = Platform.Unknown

    open val version: String = "null"
    open val versionCode: Int = -1

    open val modules: List<LocalModule> = emptyList()

    open fun installCommand() {

    }

}

class MagiskManager : RootManager() {
    override val platform: Platform = Platform.Magisk

    override val modules: List<LocalModule>
        get() = SuFile(ModId.ADB_DIR, LocalModule.MODULES_DIR)
            .listFiles().map {
                LocalModule(
                    id = it.name,
                    baseDir = ModId.ADB_DIR
                )
            }

}