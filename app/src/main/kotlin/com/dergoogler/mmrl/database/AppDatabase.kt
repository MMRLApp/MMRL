package com.dergoogler.mmrl.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import dev.dergoogler.mmrl.compat.Converters

@Database(
    entities = [
        Repo::class,
        LocalModuleUpdatable::class,
        OnlineModuleEntity::class,
        VersionItemEntity::class,
        LocalModuleEntity::class,
        BlacklistEntity::class,
    ],
    version = 15,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun repoDao(): RepoDao

    abstract fun onlineDao(): OnlineDao

    abstract fun versionDao(): VersionDao

    abstract fun localDao(): LocalDao

    abstract fun joinDao(): JoinDao

    abstract fun blacklistDao(): BlacklistDao

    companion object {
        /**
         * Only migrate data for [Repo] and [LocalModuleUpdatable]
         */
        fun build(context: Context) =
            Room
                .databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "mmrl_v2",
                ).fallbackToDestructiveMigration()
                .build()
    }
}
