package com.dergoogler.mmrl.repository

import com.dergoogler.mmrl.database.dao.BlacklistDao
import com.dergoogler.mmrl.database.dao.JoinDao
import com.dergoogler.mmrl.database.dao.LocalDao
import com.dergoogler.mmrl.database.dao.OnlineDao
import com.dergoogler.mmrl.database.dao.RepoDao
import com.dergoogler.mmrl.database.dao.VersionDao
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.database.entity.VersionItemEntity
import com.dergoogler.mmrl.database.entity.local.LocalModuleEntity
import com.dergoogler.mmrl.database.entity.local.LocalModuleUpdatable
import com.dergoogler.mmrl.database.entity.online.BlacklistEntity
import com.dergoogler.mmrl.database.entity.online.OnlineModuleEntity
import com.dergoogler.mmrl.ext.merge
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.online.Blacklist
import com.dergoogler.mmrl.model.online.OnlineModule
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val repoDao: RepoDao,
    private val onlineDao: OnlineDao,
    private val versionDao: VersionDao,
    private val localDao: LocalDao,
    private val joinDao: JoinDao,
    private val blacklistDao: BlacklistDao,
) {
    fun getLocalAllAsFlow() = localDao.getAllAsFlow().map { list ->
        list.map { it.toModule() }
    }

    fun getLocalAll() = localDao.getAll()

    fun getLocalByIdOrNullAsFlow(id: String) = localDao.getByIdOrNullAsFlow(id).map {
        it?.toModule()
    }

    suspend fun getLocalByIdOrNull(id: String) = withContext(Dispatchers.IO) {
        localDao.getByIdOrNull(id)?.toModule()
    }

    suspend fun insertLocal(value: LocalModule) = withContext(Dispatchers.IO) {
        localDao.insert(LocalModuleEntity(value))
    }

    suspend fun insertBlacklist(value: Blacklist) = withContext(Dispatchers.IO) {
        blacklistDao.insert(BlacklistEntity(value))
    }

    suspend fun deleteBlacklistById(id: String) = withContext(Dispatchers.IO) {
        blacklistDao.deleteById(id)
    }

    suspend fun getBlacklistById(id: String) = withContext(Dispatchers.IO) {
        blacklistDao.getBlacklistEntry(id)?.toBlacklist()
    }

    fun getBlacklistByIdOrNullAsFlow(id: String) = blacklistDao.getBlacklistEntryAsFlow(id).map {
        it?.toBlacklist()
    }

    fun getAllBlacklistEntriesAsFlow() = blacklistDao.getAllBlacklistEntriesAsFlow().map {
        it.map { entity -> entity.toBlacklist() }
    }

    suspend fun insertLocal(list: List<LocalModule>) = withContext(Dispatchers.IO) {
        localDao.insert(list.map { LocalModuleEntity(it) })
    }

    suspend fun deleteLocalAll() = withContext(Dispatchers.IO) {
        localDao.deleteAll()
    }

    suspend fun insertUpdatableTag(id: String, updatable: Boolean) = withContext(Dispatchers.IO) {
        localDao.insertUpdatableTag(
            LocalModuleUpdatable(
                id = id,
                updatable = updatable
            )
        )
    }

    suspend fun hasUpdatableTag(id: String) = withContext(Dispatchers.IO) {
        localDao.hasUpdatableTagOrNull(id)?.updatable != false
    }

    suspend fun clearUpdatableTag(new: List<String>) = withContext(Dispatchers.IO) {
        val removed = localDao.getUpdatableTagAll().filter { it.id !in new }
        localDao.deleteUpdatableTag(removed)
    }

    fun getRepoAllAsFlow() = repoDao.getAllAsFlow()

    suspend fun getRepoAll() = withContext(Dispatchers.IO) {
        repoDao.getAll()
    }

    suspend fun getRepoByUrl(url: String) = withContext(Dispatchers.IO) {
        repoDao.getByUrl(url)
    }

    fun getRepoByUrlAsFlow(url: String) = repoDao.getByUrlAsFlow(url)

    suspend fun insertRepo(value: Repo) = withContext(Dispatchers.IO) {
        repoDao.insert(value)
    }

    suspend fun deleteRepo(value: Repo) = withContext(Dispatchers.IO) {
        repoDao.delete(value)
    }

    fun getOnlineAllAsFlow(duplicates: Boolean = false) = joinDao.getOnlineAllAsFlow().map { list ->
        if (duplicates) {
            return@map list.map { it.toModule().copy(versions = getVersionById(it.id)) }
        }

        val values = mutableListOf<OnlineModule>()
        list.forEach { entity ->
            val new = entity.toModule()

            if (new in values) {
                val old = values.first { it.id == new.id }
                if (new.versionCode > old.versionCode) {
                    values.remove(old)
                    values.add(new.copy(versions = old.versions))
                }
            } else {
                values.add(
                    new.copy(versions = getVersionById(new.id))
                )
            }
        }
        values
    }

    fun getOnlineAllByUrlAsFlow(repoUrl: String) =
        joinDao.getOnlineAllByUrlAsFlow(repoUrl).map { list ->
            val values = mutableListOf<OnlineModule>()
            list.forEach { entity ->
                val new = entity.toModule()

                if (new in values) {
                    val old = values.first { it.id == new.id }
                    if (new.versionCode > old.versionCode) {
                        values.remove(old)
                        values.add(new.copy(versions = old.versions))
                    }
                } else {
                    values.add(
                        new.copy(versions = getVersionByIdAndUrl(new.id, repoUrl))
                    )
                }
            }

            return@map values
        }

    suspend fun getOnlineByIdAndUrl(id: String, repoUrl: String) = withContext(Dispatchers.IO) {
        joinDao.getOnlineByIdAndUrl(id, repoUrl).toModule()
    }

    suspend fun getOnlineAllById(id: String) = withContext(Dispatchers.IO) {
        onlineDao.getAllById(id).map { it.toModule() }
    }

    suspend fun getOnlineAllByUrl(url: String) = withContext(Dispatchers.IO) {
        onlineDao.getAllByUrl(url).map { it.toModule() }
    }

    suspend fun getOnlineAllByIdAndUrl(id: String, repoUrl: String) = withContext(Dispatchers.IO) {
        onlineDao.getAllByIdAndUrl(id, repoUrl).map { it.toModule() }
    }

    suspend fun insertOnline(list: List<OnlineModule>, repoUrl: String) =
        withContext(Dispatchers.IO) {
            val versions = list.map { module ->
                module.versions.map {
                    VersionItemEntity(
                        original = it,
                        id = module.id,
                        repoUrl = repoUrl
                    )
                }
            }.merge()

            versionDao.insert(versions)
            onlineDao.insert(
                list.map {
                    OnlineModuleEntity(
                        original = it,
                        repoUrl = repoUrl,
                        blacklist = getBlacklistById(it.id) ?: Blacklist.EMPTY
                    )
                }
            )
        }

    suspend fun deleteOnlineByUrl(repoUrl: String) = withContext(Dispatchers.IO) {
        versionDao.deleteByUrl(repoUrl)
        onlineDao.deleteByUrl(repoUrl)
    }

    suspend fun getVersionById(id: String) = withContext(Dispatchers.IO) {
        joinDao.getVersionById(id).map { it.toItem() }
    }

    suspend fun getVersionByIdAndUrl(id: String, repoUrl: String) = withContext(Dispatchers.IO) {
        joinDao.getVersionByIdAndUrl(id, repoUrl).map { it.toItem() }
    }
}
