package com.dergoogler.mmrl.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dergoogler.mmrl.database.entity.Repo
import kotlinx.coroutines.flow.Flow

@Dao
interface RepoDao {
    @Query("SELECT * FROM repos")
    fun getAllAsFlow(): Flow<List<Repo>>

    @Query("SELECT * FROM repos")
    suspend fun getAll(): List<Repo>

    @Query("SELECT * FROM repos WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): Repo

    @Query("SELECT * FROM repos WHERE url = :url LIMIT 1")
    fun getByUrlAsFlow(url: String): Flow<Repo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: Repo)

    @Delete
    suspend fun delete(value: Repo)
}