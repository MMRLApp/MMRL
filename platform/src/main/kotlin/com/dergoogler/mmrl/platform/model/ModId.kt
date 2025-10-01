package com.dergoogler.mmrl.platform.model

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.annotation.Keep
import com.dergoogler.mmrl.platform.file.SuFile
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Keep
@Parcelize
@Serializable
@JsonClass(generateAdapter = true)
data class ModId(
    val id: String,
    val baseDir: String = ADB_DIR,
) : Parcelable {
    val sanitizedId: String
        get() {
            return id.replace(Regex("[^a-zA-Z0-9_]"), "_")
        }

    val sanitizedIdWithFile
        get(): String {
            return "$${
                when {
                    sanitizedId.length >= 2 -> sanitizedId[0].uppercase() + sanitizedId[1]
                    sanitizedId.isNotEmpty() -> sanitizedId[0].uppercase()
                    else -> ""
                }
            }File"
        }

    val sanitizedIdWithFileInputStream get(): String = "${sanitizedIdWithFile}InputStream"

    override fun toString(): String = id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModId) return false
        return id == other.id
    }

    fun equals(other: String?, ignoreCase: Boolean = false): Boolean {
        if (!ignoreCase) {
            return id == other
        }

        return (id as java.lang.String).equalsIgnoreCase(other)
    }


    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        const val INTENT_MOD_ID_AS_PARCELABLE = "MOD_ID_AS_PARCELABLE"
        const val INTENT_MOD_ID = "MOD_ID"
        const val INTENT_ID = "id"
        const val INTENT_BASE_DIR = "BASE_DIR"

        fun String.toModId(baseDir: String = ADB_DIR): ModId {
            return ModId(this, baseDir)
        }

        val EMPTY = ModId("")

        /**
         * Retrieves a [ModId] from the Intent's extras.
         *
         * This function attempts to retrieve the ModId using three different keys in order:
         * 1. [INTENT_MOD_ID] (as a String)
         * 2. [INTENT_ID] (as a String)
         * 3. [INTENT_MOD_ID_AS_PARCELABLE] (as a Parcelable ModId)
         *
         * It handles API level differences for retrieving Parcelable extras.
         *
         * @return The [ModId] found in the Intent, or `null` if no ModId is found
         * using any of the supported keys.
         */
        fun Intent.getModId(): ModId? {
            val baseDir = getStringExtra(INTENT_BASE_DIR) ?: ADB_DIR
            val modId = getStringExtra(INTENT_MOD_ID)
            val id = getStringExtra(INTENT_ID)

            if (modId != null) {
                return modId.toModId(baseDir)
            }

            if (id != null) {
                return id.toModId(baseDir)
            }

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.getParcelableExtra(INTENT_MOD_ID_AS_PARCELABLE, ModId::class.java)
            } else {
                @Suppress("DEPRECATION")
                this.getParcelableExtra<ModId>(INTENT_MOD_ID_AS_PARCELABLE)
            }
        }

        /**
         * Puts a [ModId] object into the Intent as an extra.
         *
         * This function stores the [ModId] under the key [INTENT_MOD_ID_AS_PARCELABLE].
         *
         * @param id The [ModId] to be put into the Intent.
         */
        fun Intent.putModId(id: ModId) {
            putExtra(INTENT_MOD_ID_AS_PARCELABLE, id)
        }

        fun Intent.putBaseDir(dir: String) {
            putExtra(INTENT_BASE_DIR, dir)
        }

        /**
         * Puts a ModId string into the Intent as an extra.
         *
         * This function stores the ModId string under the key [INTENT_MOD_ID].
         *
         * @param id The ModId string to be put into the Intent.
         */
        fun Intent.putModId(id: String) {
            putExtra(INTENT_MOD_ID, id)
        }

        /**
         * Checks if the [ModId] is null or its underlying [id] string is empty.
         *
         * This function uses contracts to provide smart casting benefits to the caller.
         * If this function returns `false`, the compiler will know that the `ModId` instance
         * is not null.
         *
         * @return `true` if the [ModId] is null or its [id] is empty, `false` otherwise.
         */
        @OptIn(ExperimentalContracts::class)
        fun ModId?.isNullOrEmpty(): Boolean {
            contract {
                returns(false) implies (this@isNullOrEmpty != null)
            }

            return this == null || this.id.isEmpty()
        }

        /**
         * A list of all service-related files associated with this Magisk module.
         * These files are typically shell scripts or directories that are executed
         * or utilized by the Magisk framework during different stages of the boot process
         * or when specific actions are triggered.
         *
         * The list includes:
         * - [actionFile]: Script executed when the module is enabled/disabled.
         * - [serviceFile]: Script executed during the late_start service mode.
         * - [postFsDataFile]: Script executed after the /data partition is mounted.
         * - [postMountFile]: Script executed after all partitions are mounted.
         * - [webrootDir]: Directory for web content served by the module.
         * - [bootCompletedFile]: Script executed after the boot process is fully completed.
         * - [sepolicyFile]: File containing SEPolicy rules for the module.
         */
        @get:Keep
        val ModId.serviceFiles
            get() = listOf(
                actionFile,
                serviceFile,
                postFsDataFile,
                postMountFile,
                webrootDir,
                bootCompletedFile,
                sepolicyFile
            )

        /**
         * A list of all essential files associated with this Magisk module.
         * This includes service-related files, the uninstall script, the system directory,
         * and the module properties file.
         */
        @get:Keep
        val ModId.files
            get() = listOf(
                *serviceFiles.toTypedArray(),
                uninstallFile,
                systemPropFile,
                systemDir,
                propFile,
                disableFile,
                removeFile,
                updateFile
            )

        val ModId.adbDir get() = SuFile(baseDir)
        val ModId.configDir get() = SuFile(adbDir, HIDDEN_CONFIG_DIR)
        val ModId.moduleConfigDir get() = SuFile(configDir, id)
        val ModId.modulesDir get() = SuFile(adbDir, MODULES_DIR)
        val ModId.moduleDir get() = SuFile(modulesDir, id)
        val ModId.webrootDir get() = SuFile(moduleDir, WEBROOT_DIR)
        val ModId.modconfDir get() = SuFile(moduleDir, MODCONF_DIR)
        val ModId.modconfDependenciesDir get() = SuFile(modconfDir, MODCONF_DEPENDENCIES_DIR)
        val ModId.propFile get() = SuFile(moduleDir, PROP_FILE)
        val ModId.actionFile get() = SuFile(moduleDir, ACTION_FILE)
        val ModId.serviceFile get() = SuFile(moduleDir, SERVICE_FILE)
        val ModId.postFsDataFile get() = SuFile(moduleDir, POST_FS_DATA_FILE)
        val ModId.postMountFile get() = SuFile(moduleDir, POST_MOUNT_FILE)
        val ModId.systemPropFile get() = SuFile(moduleDir, SYSTEM_PROP_FILE)
        val ModId.bootCompletedFile get() = SuFile(moduleDir, BOOT_COMPLETED_FILE)
        val ModId.sepolicyFile get() = SuFile(moduleDir, SE_POLICY_FILE)
        val ModId.uninstallFile get() = SuFile(moduleDir, UNINSTALL_FILE)
        val ModId.systemDir get() = SuFile(moduleDir, SYSTEM_DIR)
        val ModId.disableFile get() = SuFile(moduleDir, DISABLE_FILE)
        val ModId.removeFile get() = SuFile(moduleDir, REMOVE_FILE)
        val ModId.updateFile get() = SuFile(moduleDir, UPDATE_FILE)

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
