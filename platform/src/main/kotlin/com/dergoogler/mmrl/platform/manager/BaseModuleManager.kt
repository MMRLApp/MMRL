package com.dergoogler.mmrl.platform.manager

import com.dergoogler.mmrl.platform.content.LocalModule
import com.dergoogler.mmrl.platform.content.State
import com.dergoogler.mmrl.platform.file.ExtFile
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.disableFile
import com.dergoogler.mmrl.platform.model.ModId.Companion.files
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.propFile
import com.dergoogler.mmrl.platform.model.ModId.Companion.removeFile
import com.dergoogler.mmrl.platform.model.ModId.Companion.updateFile
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.util.Shell.exec
import org.apache.commons.compress.archivers.zip.ZipFile

abstract class BaseModuleManager() : IModuleManager.Stub() {

    protected val mVersion by lazy {
        "su -v".exec().getOrDefault("unknown")
    }

    protected val mVersionCode by lazy {
        "su -V".exec().getOrDefault("").toIntOr(-1)
    }

    override fun reboot(reason: String) {
        if (reason == "recovery") {
            "/system/bin/input keyevent 26".exec()
        }

        "/system/bin/svc power reboot $reason || /system/bin/reboot $reason".exec()
    }

    override fun getModules() = ExtFile(ModId.ADB_DIR, ModId.MODULES_DIR).listFiles()
        .orEmpty()
        .mapNotNull { dir ->
            val id = ModId(dir.name)
            id.readProps?.toModule()
        }

    override fun getModuleById(id: ModId): LocalModule? = id.readProps?.toModule()

    override fun getModuleInfo(zipPath: String): LocalModule? {
        val zipFile = ZipFile.Builder().setFile(zipPath).get()
        val entry = zipFile.getEntry(ModId.PROP_FILE) ?: return null

        return zipFile.getInputStream(entry).use {
            it.bufferedReader()
                .readText()
                .let(::readProps)
                .toModule()
        }
    }

    protected fun readProps(props: String) = props.lines()
        .associate { line ->
            val items = line.split("=", limit = 2).map { it.trim() }
            if (items.size != 2) {
                "" to ""
            } else {
                items[0] to items[1]
            }
        }

    protected val ModId.readProps
        get() =
            propFile.let {
                when {
                    it.exists() -> readProps(it.readText())
                    else -> null
                }
            }

    protected val ModId.readState
        get(): State {
            removeFile.apply {
                if (exists()) return State.REMOVE
            }

            disableFile.apply {
                if (exists()) return State.DISABLE
            }

            updateFile.apply {
                if (exists()) return State.UPDATE
            }

            return State.ENABLE
        }

    protected fun readLastUpdated(id: ModId): Long {
        id.files.forEach {
            if (it.exists()) {
                return it.lastModified()
            }
        }

        return 0L
    }

    protected fun Map<String, String>.toModule(baseDir: String = ModId.ADB_DIR): LocalModule {
        val id = ModId(getOrDefault("id", "unknown"), baseDir)

        val size = id.moduleDir.length(
            recursive = true,
            skipSymLinks = true
        )

        return LocalModule(
            id = id,
            name = getOrDefault("name", id.id),
            version = getOrDefault("version", ""),
            versionCode = getOrDefault("versionCode", "-1").toIntOr(-1),
            author = getOrDefault("author", ""),
            description = getOrDefault("description", ""),
            updateJson = getOrDefault("updateJson", ""),
            state = id.readState,
            size = size,
            lastUpdated = readLastUpdated(id)
        )
    }

    protected fun String.toIntOr(defaultValue: Int) =
        runCatching {
            toInt()
        }.getOrDefault(defaultValue)
}