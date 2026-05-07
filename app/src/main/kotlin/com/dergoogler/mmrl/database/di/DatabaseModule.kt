package com.dergoogler.mmrl.database.di

import android.content.Context
import com.dergoogler.mmrl.database.AppDatabase
import com.dergoogler.mmrl.database.dao.BlacklistDao
import com.dergoogler.mmrl.database.dao.JoinDao
import com.dergoogler.mmrl.database.dao.LocalDao
import com.dergoogler.mmrl.database.dao.OnlineDao
import com.dergoogler.mmrl.database.dao.RepoDao
import com.dergoogler.mmrl.database.dao.VersionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = AppDatabase.build(context)

    @Provides
    @Singleton
    fun providesRepoDao(db: AppDatabase): RepoDao = db.repoDao()

    @Provides
    @Singleton
    fun providesOnlineDao(db: AppDatabase): OnlineDao = db.onlineDao()

    @Provides
    @Singleton
    fun providesVersionDao(db: AppDatabase): VersionDao = db.versionDao()

    @Provides
    @Singleton
    fun providesLocalDao(db: AppDatabase): LocalDao = db.localDao()

    @Provides
    @Singleton
    fun providesJoinDao(db: AppDatabase): JoinDao = db.joinDao()

    @Provides
    @Singleton
    fun providesBlacklistDao(db: AppDatabase): BlacklistDao = db.blacklistDao()
}
