package com.dergoogler.mmrl.ui.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.datastore.model.Option
import com.dergoogler.mmrl.datastore.providable.LocalUserPreferences
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.model.state.OnlineState
import com.dergoogler.mmrl.model.state.OnlineState.Companion.createState
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.model.ModId.Companion.toModId
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue

@Composable
fun rememberOnlineModules(
    repo: Repo,
    query: String = "",
): State<List<Pair<OnlineState, OnlineModule>>> {
    val localRepository = rememberLocalRepository()
    val prefs = LocalUserPreferences.current
    val repositoryMenu = remember(prefs) {
        prefs.repositoryMenu
    }

    return produceState(
        initialValue = emptyList(),
        repo, repositoryMenu, query, localRepository
    ) {
        val modules = localRepository.getOnlineAllByUrl(repo.url)

        val sorted = modules.map {
            it.createState(
                local = localRepository.getLocalByIdOrNull(it.id),
                hasUpdatableTag = localRepository.hasUpdatableTag(it.id)
            ) to it
        }.sortedWith(
            if (repositoryMenu.descending) {
                when (repositoryMenu.option) {
                    Option.Name -> compareByDescending { it.second.name.lowercase() }
                    Option.UpdatedTime -> compareBy { it.first.lastUpdated }
                    Option.Size -> compareByDescending { 0 }
                }
            } else {
                when (repositoryMenu.option) {
                    Option.Name -> compareBy { it.second.name.lowercase() }
                    Option.UpdatedTime -> compareByDescending { it.first.lastUpdated }
                    Option.Size -> compareBy { 0 }
                }
            }
        ).let { list ->
            var result = list
            if (repositoryMenu.pinInstalled) {
                result = result.sortedByDescending { it.first.installed }
            }
            if (repositoryMenu.pinUpdatable) {
                result = result.sortedByDescending { it.first.updatable }
            }
            result
        }

        val newKey = when {
            query.startsWith("id:", ignoreCase = true) -> query.removePrefix("id:")
            query.startsWith("name:", ignoreCase = true) -> query.removePrefix("name:")
            query.startsWith("author:", ignoreCase = true) -> query.removePrefix("author:")
            query.startsWith(
                "category:",
                ignoreCase = true
            ) -> query.removePrefix("category:")

            else -> query
        }.trim()

        value = sorted.filter { (_, m) ->
            if (query.isNotBlank() || newKey.isNotBlank()) {
                when {
                    query.startsWith("id:", ignoreCase = true) -> m.id.equals(
                        newKey,
                        ignoreCase = true
                    )

                    query.startsWith("name:", ignoreCase = true) -> m.name.equals(
                        newKey,
                        ignoreCase = true
                    )

                    query.startsWith("author:", ignoreCase = true) -> m.author.equals(
                        newKey,
                        ignoreCase = true
                    )

                    query.startsWith("category:", ignoreCase = true) ->
                        m.categories?.any { it.equals(newKey, ignoreCase = true) } ?: false

                    else -> m.name.contains(query, ignoreCase = true) ||
                            m.author.contains(query, ignoreCase = true) ||
                            m.description?.contains(query, ignoreCase = true) == true
                }
            } else true
        }
    }
}

@Composable
fun rememberOnlineModule(
    id: ModId,
    repo: Repo,
): State<Pair<OnlineState, OnlineModule>?> {
    val onlineModules by rememberOnlineModules(repo)
    return remember(onlineModules, id) {
        derivedStateOf {
            onlineModules.find { it.second.id.toModId() == id }
        }
    }
}
