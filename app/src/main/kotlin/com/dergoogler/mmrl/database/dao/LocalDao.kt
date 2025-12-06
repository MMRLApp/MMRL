package com.dergoogler.mmrl.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dergoogler.mmrl.database.entity.local.LocalModuleEntity
import com.dergoogler.mmrl.database.entity.local.LocalModuleUpdatable
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalDao {
    @Query("SELECT * FROM localModules")
    fun getAllAsFlow(): Flow<List<LocalModuleEntity>>

    @Query("SELECT * FROM localModules")
    fun getAll(): List<LocalModuleEntity>

    @Query("SELECT * FROM localModules WHERE id = :id LIMIT 1")
    suspend fun getByIdOrNull(id: String): LocalModuleEntity?

    @Query("SELECT * FROM localModules WHERE id = :id LIMIT 1")
    fun getByIdOrNullAsFlow(id: String): Flow<LocalModuleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: LocalModuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<LocalModuleEntity>)

    @Query("DELETE FROM localModules")
    suspend fun deleteAll()

    @Query("SELECT * FROM localModules_updatable")
    suspend fun getUpdatableTagAll(): List<LocalModuleUpdatable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdatableTag(value: LocalModuleUpdatable)

    @Query("SELECT * FROM localModules_updatable WHERE id = :id LIMIT 1")
    suspend fun hasUpdatableTagOrNull(id: String): LocalModuleUpdatable?

    @Delete
    suspend fun deleteUpdatableTag(values: List<LocalModuleUpdatable>)
}
