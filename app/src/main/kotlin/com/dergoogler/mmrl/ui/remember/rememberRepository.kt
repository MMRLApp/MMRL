package com.dergoogler.mmrl.ui.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dergoogler.mmrl.datastore.UserPreferencesRepository
import com.dergoogler.mmrl.repository.LocalRepository
import com.dergoogler.mmrl.repository.ModulesRepository
import com.dergoogler.mmrl.repository.RepositoryEntryPoints
import dagger.hilt.android.EntryPointAccessors
import kotlin.jvm.java

@Composable
fun rememberRepository(): RepositoryEntryPoints {
    val context = LocalContext.current
    return remember(Unit) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            RepositoryEntryPoints::class.java
        )
    }
}

@Composable
fun rememberLocalRepository(): LocalRepository {
    val repository = rememberRepository()
    return remember(repository) { repository.localRepository() }
}

@Composable
fun rememberModulesRepository(): ModulesRepository {
    val repository = rememberRepository()
    return remember(repository) { repository.modulesRepository() }
}

@Composable
fun rememberUserPreferencesRepository(): UserPreferencesRepository {
    val repository = rememberRepository()
    return remember(repository) { repository.userPreferencesRepository() }
}

