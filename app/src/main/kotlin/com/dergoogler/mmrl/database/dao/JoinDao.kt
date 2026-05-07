package com.dergoogler.mmrl.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.dergoogler.mmrl.database.entity.VersionItemEntity
import com.dergoogler.mmrl.database.entity.online.OnlineModuleEntity
import com.dergoogler.mmrl.model.online.ModulesJson
import kotlinx.coroutines.flow.Flow

@Dao
interface JoinDao {
    @Query(
        "SELECT m.* " +
            "FROM onlineModules m " +
            "JOIN repos r ON m.repoUrl = r.url " +
            "WHERE r.enable = 1 AND r.version = :version",
    )
    fun getOnlineAllAsFlow(version: Int = ModulesJson.CURRENT_VERSION): Flow<List<OnlineModuleEntity>>

    @Query(
        "SELECT m.* " +
            "FROM onlineModules m " +
            "JOIN repos r ON m.repoUrl = r.url " +
            "WHERE r.enable = 1 AND m.repoUrl = :repoUrl AND r.version = :version",
    )
    fun getOnlineAllByUrlAsFlow(
        repoUrl: String,
        version: Int = ModulesJson.CURRENT_VERSION,
    ): Flow<List<OnlineModuleEntity>>

    @Query(
        "SELECT m.* " +
            "FROM onlineModules m " +
            "JOIN repos r ON m.repoUrl = r.url " +
            "WHERE m.id = :id AND m.repoUrl = :repoUrl AND r.enable = 1 AND r.version = :version LIMIT 1",
    )
    suspend fun getOnlineByIdAndUrl(
        id: String,
        repoUrl: String,
        version: Int = ModulesJson.CURRENT_VERSION,
    ): OnlineModuleEntity

    @Query(
        "SELECT v.* " +
            "FROM versions v " +
            "JOIN repos r ON v.repoUrl = r.url " +
            "WHERE v.id = :id AND r.enable = 1 AND r.version = :version ORDER BY v.versionCode DESC",
    )
    suspend fun getVersionById(
        id: String,
        version: Int = ModulesJson.CURRENT_VERSION,
    ): List<VersionItemEntity>

    @Query(
        "SELECT v.* " +
            "FROM versions v " +
            "JOIN repos r ON v.repoUrl = r.url " +
            "WHERE v.id = :id AND v.repoUrl = :repoUrl AND r.enable = 1 AND r.version = :version ORDER BY v.versionCode DESC",
    )
    suspend fun getVersionByIdAndUrl(
        id: String,
        repoUrl: String,
        version: Int = ModulesJson.CURRENT_VERSION,
    ): List<VersionItemEntity>
}
