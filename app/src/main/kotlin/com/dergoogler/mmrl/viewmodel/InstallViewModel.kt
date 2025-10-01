package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const.CLEAR_CMD
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.compat.MediaStoreCompat.copyToDir
import com.dergoogler.mmrl.compat.MediaStoreCompat.getPathForUri
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.tmpDir
import com.dergoogler.mmrl.ext.toFormattedDateSafely
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.online.Blacklist
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.content.BulkModule
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toFormattedFileSize
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.topjohnwu.superuser.CallbackList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class InstallViewModel @Inject constructor(
    application: Application,
    localRepository: LocalRepository,
    modulesRepository: ModulesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : TerminalViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
    val logfile get() = "Install_${LocalDateTime.now()}.log"

    init {
        Timber.d("InstallViewModel initialized")
    }

    private val stdoutCallbackList = object : CallbackList<String?>() {
        override fun onAddElement(msg: String?) {
            if (msg == null) return

            viewModelScope.launch {
                log(msg)
            }
        }
    }

    private val stderrCallbackList = object : CallbackList<String?>() {
        override fun onAddElement(msg: String?) {
            if (msg == null) return

            viewModelScope.launch {
                devLog(msg)
            }
        }
    }

    suspend fun installModules(uris: List<Uri>) {
        if (!platformReadyDeferred.await()) {
            log(R.string.platform_initialization_failed_cannot_install)
            terminal.event = Event.FAILED
            return
        }

        if (!PlatformManager.isAlive) {
            log(R.string.platform_not_alive_cannot_install)
            terminal.event = Event.FAILED
            return
        }

        val userPreferences = userPreferencesRepository.data.first()
        terminal.event = Event.LOADING
        var allSucceeded = true

        val processedModules = mutableListOf<Pair<Uri, BulkModule?>>()
        var blacklistedModuleFound = false

        withContext(Dispatchers.IO) {
            for (uri in uris) {
                val path = context.getPathForUri(uri)
                if (path == null) {
                    withContext(Dispatchers.Main) {
                        devLog(
                            R.string.unable_to_find_path_for_uri,
                            uri.toString()
                        )
                    }
                    processedModules.add(uri to null)
                    allSucceeded = false
                    continue
                }

                if (userPreferences.strictMode && !path.endsWith(".zip")) {
                    withContext(Dispatchers.Main) {
                        log(
                            R.string.is_not_a_module_file_magisk_modules_must_be_zip_files_skipping,
                            path
                        )
                    }
                    processedModules.add(uri to null)
                    allSucceeded = false
                    continue
                }

                val info = PlatformManager.moduleManager.getModuleInfo(path)
                if (info == null) {
                    withContext(Dispatchers.Main) {
                        devLog(
                            R.string.unable_to_gather_module_info_of_file,
                            path
                        )
                    }
                    processedModules.add(uri to null)
                    allSucceeded = false
                    continue
                }

                val blacklist = getBlacklistById(info.id.toString())
                if (Blacklist.isBlacklisted(userPreferences.blacklistAlerts, blacklist)) {
                    withContext(Dispatchers.Main) {
                        log(R.string.cannot_install_blacklisted_modules_settings_security_blacklist_alerts)
                        terminal.event = Event.FAILED
                    }
                    allSucceeded = false
                    blacklistedModuleFound = true
                    break
                }
                processedModules.add(uri to BulkModule(id = info.id.toString(), name = info.name))
            }
        }

        if (blacklistedModuleFound) {
            return
        }

        val validBulkModules = processedModules.mapNotNull { it.second }

        for (item in processedModules) {
            val uri = item.first
            val bulkModuleInfo = item.second
            if (bulkModuleInfo == null) {
                continue
            }

            if (userPreferences.clearInstallTerminal && uris.size > 1) {
                log(CLEAR_CMD)
            }

            val result = loadAndInstallModule(uri, validBulkModules)
            if (!result) {
                allSucceeded = false
                withContext(Dispatchers.Main) {
                    log(context.getString(R.string.installation_aborted_due_to_an_error))
                }
                break
            }
        }
        terminal.event = if (allSucceeded) Event.SUCCEEDED else Event.FAILED
    }

    private val datePattern = runBlocking { userPreferencesRepository.data.first().datePattern }

    private suspend fun loadAndInstallModule(
        uri: Uri,
        allBulkModulesInBatch: List<BulkModule>,
    ): Boolean = withContext(Dispatchers.IO) {
        val path = context.getPathForUri(uri)

        if (path != null) {
            val moduleInfoFromPath = PlatformManager.moduleManager.getModuleInfo(path)
            if (moduleInfoFromPath != null) {
                withContext<Unit>(Dispatchers.Main) {
                    moduleInfoFromPath.let { mod ->
                        devLog(R.string.install_view_module_info)
                        devLog("ID: ${mod.id.id}")
                        devLog("Name: ${mod.name}")
                        devLog("Version: ${mod.version}")
                        devLog("Version Code: ${mod.versionCode}")
                        devLog("Author: ${mod.author}")
                        devLog("Description: ${mod.description}")
                        devLog("Update JSON: ${mod.updateJson}")
                        devLog("State: ${mod.state}")
                        devLog("Size: ${mod.size.toFormattedFileSize()}")
                        devLog("Last Updated: ${mod.lastUpdated.toFormattedDateSafely(datePattern)}")
                        devLog("::endgroup::")
                    }
                }
                return@withContext install(path, allBulkModulesInBatch, moduleInfoFromPath)
            }
        }

        withContext(Dispatchers.Main) { log(R.string.copying_zip_to_temp_directory) }
        val tmpFile = context.copyToDir(uri, context.tmpDir) ?: run {
            withContext(Dispatchers.Main) {
                terminal.event = Event.FAILED
                log(context.getString(R.string.copying_failed))
            }
            return@withContext false
        }

        val moduleInfoFromTmp = PlatformManager.moduleManager.getModuleInfo(tmpFile.path)
        if (moduleInfoFromTmp == null) {
            withContext(Dispatchers.Main) {
                terminal.event = Event.FAILED
                log(R.string.unable_to_gather_module_info)
            }
            tmpFile.delete()
            return@withContext false
        }

        withContext(Dispatchers.Main) {
            devLog(R.string.install_view_module_info, moduleInfoFromTmp.toString())
        }
        return@withContext install(tmpFile.path, allBulkModulesInBatch, moduleInfoFromTmp)
    }

    private suspend fun install(
        zipPath: String,
        allBulkModulesInBatch: List<BulkModule>,
        module: LocalModule? = null,
    ): Boolean = withContext(Dispatchers.Default) {
        val zipFile = File(zipPath)
        val userPreferences = userPreferencesRepository.data.first()

        val installCommand = PlatformManager.moduleManager.getInstallCommand(zipPath)
        if (installCommand.isNullOrBlank()) {
            withContext(Dispatchers.Main) {
                log("Error: Failed to get install command for ${zipFile.name}")
            }
            return@withContext false
        }

        val cmds = listOf(
            "export ASH_STANDALONE=1",
            "export MMRL=true",
            "export MMRL_VER=${BuildConfig.VERSION_NAME}",
            "export MMRL_VER_CODE=${BuildConfig.VERSION_CODE}",
            "export BULK_MODULES=\"${allBulkModulesInBatch.joinToString(" ") { it.id }}\"",
            installCommand
        )

        withContext(Dispatchers.Main) {
            log(R.string.install_view_installing, zipFile.name)
        }

        if (!terminal.shell.isAlive) {
            withContext(Dispatchers.Main) {
                log("Error: Shell is not alive. Cannot execute installation.")
            }
            return@withContext false
        }

        val result = terminal.shell.newJob()
            .add(*cmds.toTypedArray())
            .to(stdoutCallbackList, stderrCallbackList)
            .exec()

        val success = result.isSuccess
        if (success) {
            module.nullable(::insertLocal)

            if (userPreferences.deleteZipFile) {
                deleteBySu(zipPath)
            }
        } else {
            withContext(Dispatchers.Main) {
                log("Error: Installation failed for ${zipFile.name}. Exit code: ${result.code}")
                result.err.forEach { log("Shell Error: $it") }
            }
            if (module != null && !terminal.shell.isAlive) {
                withContext(Dispatchers.IO) {
                    runCatching {
                        SuFile("/data/adb/modules_update/${module.id}").deleteRecursively()
                    }.onFailure {
                        Timber.e(
                            it,
                            "Failed to cleanup /data/adb/modules_update/${module.id}"
                        )
                    }
                }
            }
        }
        return@withContext success
    }

    private suspend fun insertLocal(module: LocalModule) {
        withContext(Dispatchers.IO) {
            localRepository.insertLocal(module)
        }
    }

    private fun deleteBySu(zipPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                PlatformManager.fileManager.deleteOnExit(zipPath)
            }.onFailure {
                Timber.e(it, "Failed to delete $zipPath via su")
                withContext(Dispatchers.Main) {
                    log("Warning: Failed to delete $zipPath after installation.")
                }
            }.onSuccess {
                Timber.d("Deleted: $zipPath")
                withContext(Dispatchers.Main) {
                    devLog(R.string.deleted_zip_file, zipPath)
                }
            }
        }
    }
}
