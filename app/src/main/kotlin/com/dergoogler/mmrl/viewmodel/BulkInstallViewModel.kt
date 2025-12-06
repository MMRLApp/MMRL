package com.dergoogler.mmrl.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.model.local.BulkModule
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.service.DownloadService
import com.dergoogler.mmrl.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BulkInstallViewModel
    @Inject
    constructor(
        application: Application,
        localRepository: LocalRepository,
        modulesRepository: ModulesRepository,
        userPreferencesRepository: UserPreferencesRepository,
    ) : MMRLViewModel(application, localRepository, modulesRepository, userPreferencesRepository) {
        private val bulkModulesFlow = MutableStateFlow(listOf<BulkModule>())
        val bulkModules get() = bulkModulesFlow.asStateFlow()

        fun addBulkModule(
            module: BulkModule,
            onSuccess: () -> Unit,
            onFailure: (error: String) -> Unit,
        ) {
            val currentModules = bulkModulesFlow.value
            if (currentModules.contains(module)) {
                onFailure(context.getString(R.string.bulk_install_module_already_added))
            } else {
                bulkModulesFlow.value = currentModules + module
                onSuccess()
            }
        }

        fun removeBulkModule(module: BulkModule) {
            bulkModulesFlow.value -= module
        }

        fun clearBulkModules() {
            bulkModulesFlow.value = listOf()
        }

        fun downloadMultiple(
            items: List<BulkModule>,
            onAllSuccess: (List<Uri>) -> Unit,
            onFailure: (Throwable) -> Unit,
        ) {
            viewModelScope.launch {
                val downloadPath = File(userPreferencesRepository.data.first().downloadPath)

                val downloadedFiles = mutableListOf<Uri>()
                val exceptions = mutableListOf<Throwable>()

                items.forEach {
                    val item = it.versionItem

                    val filename =
                        Utils.getFilename(
                            name = it.name,
                            version = item.version,
                            versionCode = item.versionCode,
                            extension = "zip",
                        )

                    val task =
                        DownloadService.TaskItem(
                            key = item.hashCode(),
                            url = item.zipUrl,
                            filename = filename,
                            title = it.name,
                            desc = item.versionDisplay,
                        )

                    val downloadDeferred = CompletableDeferred<Uri>()
                    val listener =
                        object : DownloadService.IDownloadListener {
                            override fun getProgress(value: Float) {}

                            override fun onFileExists() {
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.file_already_exists),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }

                            override fun onSuccess() {
                                val file = downloadPath.resolve(filename)
                                downloadDeferred.complete(file.toUri())
                            }

                            override fun onFailure(e: Throwable) {
                                downloadDeferred.completeExceptionally(e)
                            }
                        }

                    try {
                        DownloadService.start(
                            context = context,
                            task = task,
                            listener = listener,
                        )
                        val file = downloadDeferred.await()
                        downloadedFiles.add(file)
                    } catch (e: Throwable) {
                        exceptions.add(e)
                        Timber.d(e)
                    }
                }

                if (exceptions.isEmpty()) {
                    onAllSuccess(downloadedFiles)
                } else {
                    onFailure(exceptions.first())
                }
            }
        }

        @Composable
        fun getProgress(item: VersionItem): Float {
            val progress by DownloadService
                .getProgressByKey(item.hashCode())
                .collectAsStateWithLifecycle(initialValue = 0f)

            return progress
        }
    }
