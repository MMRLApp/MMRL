package com.dergoogler.mmrl.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dergoogler.mmrl.database.entity.online.OnlineModuleEntity

@Dao
interface OnlineDao {
    @Query("SELECT * FROM onlineModules WHERE id = :id")
    suspend fun getAllById(id: String): List<OnlineModuleEntity>

    @Query("SELECT * FROM onlineModules WHERE id = :id AND repoUrl = :repoUrl")
    suspend fun getAllByIdAndUrl(id: String, repoUrl: String): List<OnlineModuleEntity>

    @Query("SELECT * FROM onlineModules WHERE repoUrl = :repoUrl")
    suspend fun getAllByUrl(repoUrl: String): List<OnlineModuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<OnlineModuleEntity>)

    @Query("DELETE from onlineModules WHERE repoUrl = :repoUrl")
    suspend fun deleteByUrl(repoUrl: String)
}