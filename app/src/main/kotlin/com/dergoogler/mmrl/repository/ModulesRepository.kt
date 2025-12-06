package com.dergoogler.mmrl.repository

import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.network.runRequest
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.stub.IMMRLApiManager
import com.dergoogler.mmrl.stub.IRepoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModulesRepository
    @Inject
    constructor(
        private val localRepository: LocalRepository,
    ) {
        suspend fun getLocalAll() =
            withContext(Dispatchers.IO) {
                with(PlatformManager.moduleManager.modules) {
                    localRepository.deleteLocalAll()
                    localRepository.insertLocal(this)
                    localRepository.clearUpdatableTag(map { it.id.id })
                }
            }

        suspend fun getLocal(id: ModId) =
            withContext(Dispatchers.IO) {
                val module = PlatformManager.moduleManager.getModuleById(id)
                localRepository.insertLocal(module)
            }

        suspend fun getRepoAll(onlyEnable: Boolean = true) =
            localRepository
                .getRepoAll()
                .filter {
                    if (onlyEnable) it.enable else true
                }.map {
                    getRepo(it)
                }

        suspend fun getRepo(repo: Repo) =
            withContext(Dispatchers.IO) {
                runRequest {
                    val api = IRepoManager.build(repo.url)
                    return@runRequest api.modules.execute()
                }.onSuccess { modulesJson ->
                    localRepository.insertRepo(repo.copy(modulesJson))
                    localRepository.deleteOnlineByUrl(repo.url)
                    localRepository.insertOnline(
                        list = modulesJson.modules,
                        repoUrl = repo.url,
                    )
                }.onFailure {
                    Timber.e(it, "getRepo: ${repo.url}")
                }
            }

        suspend fun getBlacklist() =
            withContext(Dispatchers.IO) {
                runRequest {
                    val api = IMMRLApiManager.build()
                    return@runRequest api.blacklist.execute()
                }.onSuccess { blacklist ->
                    blacklist.map {
                        localRepository.deleteBlacklistById(it.id)
                        localRepository.insertBlacklist(it)
                    }
                }.onFailure {
                    Timber.e(it, "getBlacklist: Failed to get blacklist")
                }
            }
    }
