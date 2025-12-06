package com.dergoogler.mmrl.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dergoogler.mmrl.database.entity.VersionItemEntity

@Dao
interface VersionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<VersionItemEntity>)

    @Query("DELETE from versions WHERE repoUrl = :repoUrl")
    suspend fun deleteByUrl(repoUrl: String)
}
