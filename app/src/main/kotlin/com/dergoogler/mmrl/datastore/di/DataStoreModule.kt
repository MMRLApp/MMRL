package com.dergoogler.mmrl.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.dergoogler.mmrl.datastore.UserPreferencesSerializer
import com.dergoogler.mmrl.datastore.model.UserPreferences
import com.dergoogler.mmrl.datastore.provider.DataStoreProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun providesUserPreferencesDataStore(
        @ApplicationContext context: Context,
        userPreferencesSerializer: UserPreferencesSerializer,
    ): DataStore<UserPreferences> = DataStoreProvider().providesUserPreferencesDataStore(context, userPreferencesSerializer)
}
