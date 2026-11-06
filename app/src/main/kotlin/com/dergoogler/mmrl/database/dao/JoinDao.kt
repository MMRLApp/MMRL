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
        """
        SELECT m.*
        FROM onlineModules m
        JOIN repos r ON m.repoUrl = r.url
        LEFT JOIN localModules l ON l.id = m.id
        LEFT JOIN localModules_updatable u ON u.id = m.id
        WHERE r.enable = 1 AND r.version = :version AND (:repoUrl = '' OR m.repoUrl = :repoUrl)
          AND (:repoUrl != '' OR NOT EXISTS (
              SELECT 1 FROM onlineModules other
              JOIN repos otherRepo ON other.repoUrl = otherRepo.url
              WHERE other.id = m.id AND otherRepo.enable = 1 AND otherRepo.version = :version
                AND (other.versionCode > m.versionCode OR
                     (other.versionCode = m.versionCode AND other.rowid < m.rowid))
          ))
          AND (
              (:prefix = '' AND (instr(lower(m.name), lower(:search)) > 0
                  OR instr(lower(m.author), lower(:search)) > 0
                  OR instr(lower(m.description), lower(:search)) > 0))
              OR (:prefix = 'id' AND m.id = :search COLLATE NOCASE)
              OR (:prefix = 'name' AND m.name = :search COLLATE NOCASE)
              OR (:prefix = 'author' AND m.author = :search COLLATE NOCASE)
              OR (:prefix = 'category' AND m.categories LIKE :categoryPattern ESCAPE '\')
          )
        ORDER BY
          CASE WHEN :pinUpdatable AND l.author = m.author AND l.versionCode < m.versionCode
               AND (u.updatable IS NULL OR u.updatable = 1) THEN 1 ELSE 0 END DESC,
          CASE WHEN :pinInstalled AND l.author = m.author THEN 1 ELSE 0 END DESC,
          CASE WHEN :sort = 'Name' AND NOT :descending THEN lower(m.name) END ASC,
          CASE WHEN :sort = 'Name' AND :descending THEN lower(m.name) END DESC,
          CASE WHEN :sort = 'UpdatedTime' AND NOT :descending THEN COALESCE(
              (SELECT v.timestamp FROM versions v JOIN repos vr ON v.repoUrl = vr.url
               WHERE v.id = m.id AND vr.enable = 1 AND vr.version = :version
                 AND (:repoUrl = '' OR v.repoUrl = m.repoUrl)
               ORDER BY CASE WHEN :repoUrl = '' THEN v.versionCode END DESC, v.rowid LIMIT 1), 0)
          END DESC,
          CASE WHEN :sort = 'UpdatedTime' AND :descending THEN COALESCE(
              (SELECT v.timestamp FROM versions v JOIN repos vr ON v.repoUrl = vr.url
               WHERE v.id = m.id AND vr.enable = 1 AND vr.version = :version
                 AND (:repoUrl = '' OR v.repoUrl = m.repoUrl)
               ORDER BY CASE WHEN :repoUrl = '' THEN v.versionCode END DESC, v.rowid LIMIT 1), 0)
          END ASC,
          m.rowid
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun getRepositoryPage(
        repoUrl: String,
        search: String,
        prefix: String,
        categoryPattern: String,
        sort: String,
        descending: Boolean,
        pinInstalled: Boolean,
        pinUpdatable: Boolean,
        limit: Int,
        offset: Int,
        version: Int = ModulesJson.CURRENT_VERSION,
    ): List<OnlineModuleEntity>

    @Query(
        """
        SELECT (SELECT COUNT(*) FROM onlineModules) + (SELECT COUNT(*) FROM repos) +
            (SELECT COUNT(*) FROM versions) + (SELECT COUNT(*) FROM localModules) +
            (SELECT COUNT(*) FROM localModules_updatable)
        """,
    )
    fun observeRepositoryChanges(): Flow<Int>

    @Query(
        """
        SELECT v.* FROM versions v JOIN repos r ON v.repoUrl = r.url
        WHERE v.id IN (:ids) AND r.enable = 1 AND r.version = :version
          AND (:repoUrl = '' OR v.repoUrl = :repoUrl)
        ORDER BY CASE WHEN :repoUrl = '' THEN v.versionCode END DESC, v.rowid
        """,
    )
    suspend fun getRepositoryVersions(
        ids: List<String>,
        repoUrl: String,
        version: Int = ModulesJson.CURRENT_VERSION,
    ): List<VersionItemEntity>

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

    @Query("SELECT * FROM versions WHERE repoUrl = :repoUrl")
    fun getVersionsByRepoUrl(repoUrl: String): List<VersionItemEntity>
}
