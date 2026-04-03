@file:Suppress("unused")

package com.dergoogler.mmrl.model.local

import android.util.Log
import com.dergoogler.mmrl.model.local.LocalModule.Companion.ACTION_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.BOOT_COMPLETED_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.DISABLE_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.EMPTY
import com.dergoogler.mmrl.model.local.LocalModule.Companion.HIDDEN_CONFIG_DIR
import com.dergoogler.mmrl.model.local.LocalModule.Companion.MODCONF_DEPENDENCIES_DIR
import com.dergoogler.mmrl.model.local.LocalModule.Companion.MODCONF_DIR
import com.dergoogler.mmrl.model.local.LocalModule.Companion.MODULES_DIR
import com.dergoogler.mmrl.model.local.LocalModule.Companion.POST_FS_DATA_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.POST_MOUNT_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.PROP_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.REMOVE_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.SERVICE_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.SE_POLICY_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.SYSTEM_DIR
import com.dergoogler.mmrl.model.local.LocalModule.Companion.SYSTEM_PROP_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.UNINSTALL_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.UPDATE_FILE
import com.dergoogler.mmrl.model.local.LocalModule.Companion.WEBROOT_DIR
import com.dergoogler.mmrl.utils.Utils
import dev.mmrlx.nio.SuFile
import dev.mmrlx.nio.ktx.readLines
import java.io.File
import java.io.InputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

data class LocalModule(
    val id: String,
    internal val baseDir: String = ADB_DIR,
    internal val propsSource: InputStream? = null,
) {
    val name: String get() = prop("name", default = "<name>")
    val version: String get() = prop("version", default = "<version>")
    val versionCode: Int get() = prop("versionCode", default = -1)
    val author: String get() = prop("updateJson", default = "<author>")
    val description: String get() = prop("updateJson", default = "<description>")
    val updateJson: String get() = prop("updateJson", default = "")

    val state: State
        get() {
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

    val size: Long
        get() =
            with(moduleDir) {
                takeIf { exists() && isDirectory }
                    ?.walkTopDown()
                    ?.filter(File::isFile)
                    ?.sumOf(File::length)
                    ?: 0L

            }

    val lastUpdated: Long
        get() = (files + serviceFiles)
            .filter(SuFile::exists)
            .maxOfOrNull(SuFile::lastModified) ?: 0L

    private val propertiesSource: List<String>
        get() {
            if (propsSource != null) return propsSource.readLines()
            return propFile.readLines()
        }

    fun enable(): Result<LocalModule> {
        if (!modulesDir.exists()) return Result.failure(Exception("Module not found"))
        return runCatching {
            removeFile.apply { if (exists()) delete() }
            disableFile.apply { if (exists()) delete() }
            this@LocalModule
        }
    }

    fun disable(): Result<LocalModule> {
        if (!modulesDir.exists()) return Result.failure(Exception("Module not found"))
        return runCatching {
            removeFile.apply { if (exists()) delete() }
            disableFile.createNewFile()
            this@LocalModule
        }
    }

    fun remove(): Result<LocalModule> {
        if (!modulesDir.exists()) return Result.failure(Exception("Module not found"))
        return runCatching {
            disableFile.apply { if (exists()) delete() }
            removeFile.createNewFile()
            this@LocalModule
        }
    }

    private inline fun <reified T> prop(
        key: String,
        vararg alias: String,
        default: T,
    ): T {
        val props: Map<String, String?> = propertiesSource
            .mapNotNull { line ->
                val idx = line.indexOf('=')
                if (idx < 0) null else line.substring(0, idx).trim() to line.substring(idx + 1)
                    .trim()
            }
            .toMap()

        val value = (sequenceOf(key) + alias.asSequence())
            .firstNotNullOfOrNull { props[it] }
            ?: return default

        @Suppress("UNCHECKED_CAST")
        return try {
            when (T::class) {
                Int::class -> value.toInt() as T
                Boolean::class -> value.toBoolean() as T
                Double::class -> value.toDouble() as T
                String::class -> value as T
                else -> {
                    Log.e(TAG, "Unsupported prop type: ${T::class}"); default
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Prop parse failed for '$key': ${e.message}")
            default
        }
    }

    companion object {
        @PublishedApi
        internal const val TAG = "LocalModule"

        val EMPTY = LocalModule("")

        const val ADB_DIR = "/data/adb"
        const val WEBROOT_DIR = "webroot"
        const val MODCONF_DIR = "modconf"
        const val MODCONF_DEPENDENCIES_DIR = "dependencies"
        const val MODULES_DIR = "modules"
        const val HIDDEN_CONFIG_DIR = ".config"

        const val PROP_FILE = "module.prop"

        const val ACTION_FILE = "action.sh"
        const val BOOT_COMPLETED_FILE = "boot-completed.sh"
        const val SERVICE_FILE = "service.sh"
        const val POST_FS_DATA_FILE = "post-fs-data.sh"
        const val POST_MOUNT_FILE = "post-mount.sh"
        const val SYSTEM_PROP_FILE = "system.prop"
        const val SE_POLICY_FILE = "sepolicy.rule"
        const val UNINSTALL_FILE = "uninstall.sh"
        const val SYSTEM_DIR = "system"

        // State files
        const val DISABLE_FILE = "disable"
        const val REMOVE_FILE = "remove"
        const val UPDATE_FILE = "update"
    }
}

val LocalModule.hasWebUI get() = webrootDir.let { it.exists() && it.isDirectory }
val LocalModule.hasModConf get() = modconfDir.let { it.exists() && it.isDirectory }
val LocalModule.hasAction get() = actionFile.exists()
val LocalModule.hasService get() = serviceFile.exists()
val LocalModule.hasPostFsData get() = postFsDataFile.exists()
val LocalModule.hasPostMount get() = postMountFile.exists()
val LocalModule.hasSystemProp get() = systemPropFile.exists()
val LocalModule.hasBootCompleted get() = bootCompletedFile.exists()
val LocalModule.hasSepolicy get() = sepolicyFile.exists()
val LocalModule.hasUninstall get() = uninstallFile.exists()
val LocalModule.hasSystem get() = systemDir.let { it.exists() && it.isDirectory }
val LocalModule.hasDisable get() = disableFile.exists()
val LocalModule.hasRemove get() = removeFile.exists()
val LocalModule.hasUpdate get() = updateFile.exists()
val LocalModule.isEmpty get() = this == EMPTY

val LocalModule.serviceFiles
    get() =
        listOf(
            actionFile,
            serviceFile,
            postFsDataFile,
            postMountFile,
            webrootDir,
            bootCompletedFile,
            sepolicyFile,
        )

val LocalModule.files
    get() =
        listOf(
            *serviceFiles.toTypedArray(),
            uninstallFile,
            systemPropFile,
            systemDir,
            propFile,
            disableFile,
            removeFile,
            updateFile,
        )

val LocalModule.adbDir get() = SuFile(baseDir)
val LocalModule.configDir get() = SuFile(adbDir, HIDDEN_CONFIG_DIR)
val LocalModule.moduleConfigDir get() = SuFile(configDir, id)
val LocalModule.modulesDir get() = SuFile(adbDir, MODULES_DIR)
val LocalModule.moduleDir get() = SuFile(modulesDir, id)
val LocalModule.webrootDir get() = SuFile(moduleDir, WEBROOT_DIR)
val LocalModule.modconfDir get() = SuFile(moduleDir, MODCONF_DIR)
val LocalModule.modconfDependenciesDir get() = SuFile(modconfDir, MODCONF_DEPENDENCIES_DIR)
val LocalModule.propFile get() = SuFile(moduleDir, PROP_FILE)
val LocalModule.actionFile get() = SuFile(moduleDir, ACTION_FILE)
val LocalModule.serviceFile get() = SuFile(moduleDir, SERVICE_FILE)
val LocalModule.postFsDataFile get() = SuFile(moduleDir, POST_FS_DATA_FILE)
val LocalModule.postMountFile get() = SuFile(moduleDir, POST_MOUNT_FILE)
val LocalModule.systemPropFile get() = SuFile(moduleDir, SYSTEM_PROP_FILE)
val LocalModule.bootCompletedFile get() = SuFile(moduleDir, BOOT_COMPLETED_FILE)
val LocalModule.sepolicyFile get() = SuFile(moduleDir, SE_POLICY_FILE)
val LocalModule.uninstallFile get() = SuFile(moduleDir, UNINSTALL_FILE)
val LocalModule.systemDir get() = SuFile(moduleDir, SYSTEM_DIR)
val LocalModule.disableFile get() = SuFile(moduleDir, DISABLE_FILE)
val LocalModule.removeFile get() = SuFile(moduleDir, REMOVE_FILE)
val LocalModule.updateFile get() = SuFile(moduleDir, UPDATE_FILE)

val LocalModule.versionDisplay get() = Utils.getVersionDisplay(version, versionCode)

val Map<String, *>.properties
    get() = buildString {
        this@properties.forEach { (key, value) ->
            append("$key=$value\n")
        }
    }

val Map<String, *>.propertiesStream get() = this.properties.byteInputStream()


@OptIn(ExperimentalContracts::class)
inline fun <R> LocalModule?.isValid(block: (LocalModule) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (this == null) return null

    if (isEmpty) {
        return null
    }

    return block(this)
}
