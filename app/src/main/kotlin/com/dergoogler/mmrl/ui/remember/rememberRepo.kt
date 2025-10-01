package com.dergoogler.mmrl.ui.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.database.entity.Repo

@Composable
fun rememberRepos(): State<List<Repo>> {
    val localRepository = rememberLocalRepository()
    return produceState(initialValue = emptyList(), localRepository) {
        value = localRepository.getRepoAll()
    }
}

@Composable
fun rememberRepo(
    url: String,
): State<Repo?> {
    val repos by rememberRepos()
    return remember(repos) {
        derivedStateOf {
            repos.find { it.url == url }
        }
    }
}
