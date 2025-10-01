package com.dergoogler.mmrl.platform.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dergoogler.mmrl.platform.file.config.ConfigFile
import com.dergoogler.mmrl.platform.file.config.ConfigFileSave
import kotlinx.coroutines.launch

/**
 * A Composable function that remembers a [ConfigFile] instance and provides its current state
 * along with a function to update it.
 *
 * This function utilizes [rememberCoroutineScope] to launch save operations and
 * [collectAsStateWithLifecycle] to observe changes in the configuration state reactively.
 *
 * @param T The type of the configuration data.
 * @param config The [ConfigFile] instance to remember and manage.
 * @return A [Pair] containing:
 *   - The current configuration state of type [T].
 *   - A [ConfigFileSave] lambda function to update the configuration. This function
 *     takes a builder action that operates on a [MutableConfig] of the configuration type.
 *
 * @see ConfigFile
 * @see ConfigFileSave
 * @see rememberCoroutineScope
 * @see collectAsStateWithLifecycle
 */
@Composable
inline fun <reified T> rememberConfigFile(config: ConfigFile<T>): Pair<T, ConfigFileSave<T>> {
    val scope = rememberCoroutineScope()
    val configFile = remember { config }
    val config by configFile.getConfigStateFlow().collectAsStateWithLifecycle()

    val updater: ConfigFileSave<T> = fun(builderAction) {
        scope.launch {
            configFile.save(builderAction)
        }
    }

    return config to updater
}