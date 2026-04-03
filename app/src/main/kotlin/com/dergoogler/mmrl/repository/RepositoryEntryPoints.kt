package com.dergoogler.mmrl.repository

import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.manager.RootManagerRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RepositoryEntryPoints {
    fun localRepository(): LocalRepository

    fun modulesRepository(): ModulesRepository

    fun rootManager(): RootManagerRepository

    fun userPreferencesRepository(): UserPreferencesRepository
}
