package com.dergoogler.mmrl.ui.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dergoogler.mmrl.datastore.model.Option
import com.dergoogler.mmrl.datastore.providable.LocalUserPreferences
import com.dergoogler.mmrl.model.json.UpdateJson
import com.dergoogler.mmrl.model.local.ModuleAnalytics
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.model.state.OnlineState.Companion.createState
import com.dergoogler.mmrl.platform.content.LocalModule
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasAction
import com.dergoogler.mmrl.platform.content.LocalModule.Companion.hasWebUI
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.repository.LocalRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.apache.commons.lang3.ClassUtils.comparator

@Composable
fun rememberLocalModules(query: String = ""): State<List<LocalModule>> {
    val localRepository = rememberLocalRepository()
    val prefs = LocalUserPreferences.current

    val menu = remember(prefs) { prefs.modulesMenu }

    return produceState(initialValue = emptyList(), localRepository, menu, query) {
        val modules = localRepository.getLocalAllAsFlow().firstOrNull()

        if (modules == null) {
            value = emptyList()
            return@produceState
        }

        val sorted = modules.sortedWith(
            comparator(menu.option, menu.descending)
        ).let { v ->
            val a = if (menu.pinEnabled) {
                v.sortedByDescending { it.state == com.dergoogler.mmrl.model.local.State.ENABLE }
            } else {
                v
            }

            val b = if (menu.pinAction) {
                a.sortedByDescending { it.hasAction }
            } else {
                a
            }

            if (menu.pinWebUI) {
                b.sortedByDescending { it.hasWebUI }
            } else {
                b
            }
        }

        val newKey = when {
            query.startsWith("id:", ignoreCase = true) -> query.removePrefix("id:")
            query.startsWith("name:", ignoreCase = true) -> query.removePrefix("name:")
            query.startsWith("author:", ignoreCase = true) -> query.removePrefix("author:")

            else -> query
        }.trim()

        value = sorted.filter { m ->
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

                    else -> m.name.contains(query, ignoreCase = true) ||
                            m.author.contains(query, ignoreCase = true) || m.description.contains(
                        query,
                        ignoreCase = true
                    )
                }
            } else true
        }
    }
}

@Composable
fun rememberLocalModule(id: ModId): State<LocalModule?> {
    val modules by rememberLocalModules()
    return remember(modules, id) {
        derivedStateOf {
            modules.find { it.id == id }
        }
    }
}

@Composable
fun rememberUpdatableModuleCount(): State<Int> {
    val localRepository = rememberLocalRepository()

    val versionItemCache = remember {
        mutableStateMapOf<String, VersionItem?>()
    }

    return produceState(initialValue = 0, localRepository) {
        localRepository.getLocalAllAsFlow().collect { modules ->
            val updatableModules = modules.filter {
                localRepository.hasUpdatableTag(it.id.toString())
            }

            var count = 0

            for (module in updatableModules) {
                val id = module.id.toString()

                val updateVersionItem = if (module.updateJson.isNotBlank()) {
                    UpdateJson.loadToVersionItem(module.updateJson)
                } else {
                    localRepository.getVersionById(id).firstOrNull()
                }

                val installedVersionCode = module.versionCode
                val updateVersionCode = updateVersionItem?.versionCode ?: -1

                if (updateVersionCode > installedVersionCode) {
                    count++
                    versionItemCache[id] = updateVersionItem
                } else {
                    versionItemCache[id] = null
                }
            }

            value = count
        }
    }
}

@Composable
fun rememberLocalAnalytics(): State<ModuleAnalytics> {
    val context = LocalContext.current
    val modules by rememberLocalModules()
    return remember(modules) {
        derivedStateOf {
            ModuleAnalytics(
                context = context,
                local = modules
            )
        }
    }
}

private fun comparator(
    option: Option,
    descending: Boolean,
): Comparator<LocalModule> = if (descending) {
    when (option) {
        Option.Name -> compareByDescending { it.name.lowercase() }
        Option.UpdatedTime -> compareBy { it.lastUpdated }
        Option.Size -> compareByDescending { it.size }
    }

} else {
    when (option) {
        Option.Name -> compareBy { it.name.lowercase() }
        Option.UpdatedTime -> compareByDescending { it.lastUpdated }
        Option.Size -> compareBy { it.size }
    }
}