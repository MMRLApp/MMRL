@file:Suppress("CanBeParameter", "PropertyName")

package com.dergoogler.mmrl.platform.file.config

import android.util.Log
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.toMap
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.platform.util.moshi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.util.concurrent.ConcurrentHashMap

/**
 * Enum defining strategies for merging configuration files.
 */
enum class ConfigFileMergeStrategy {
    /**
     * When merging, the new value replaces the existing value.
     */
    REPLACE,

    /**
     * When merging lists, the new list is appended to the existing list.
     * For other types, it behaves like [REPLACE].
     */
    APPEND,

    /**
     * When merging lists, the new list is appended to the existing list,
     * and then duplicates are removed.
     * For other types, it behaves like [REPLACE].
     */
    DEDUPLICATE
}

/**
 * Type alias for a function that saves a configuration.
 * It takes a lambda that modifies a [MutableConfigMap] and applies it to the current configuration.
 * @param T The type of the configuration data.
 */
typealias ConfigFileSave<T> = (MutableConfigMap<Any?>.(T) -> Unit) -> Unit

interface IConfig<T> {
    /**
     * Gets the unique identifier for the module.
     * This ID is used for caching and file naming.
     *
     * @return The [ModId] of the module.
     */
    fun getModuleId(): ModId

    /**
     * Provides the primary configuration file for a given module ID.
     *
     * @param id The [ModId] identifying the module.
     * @return A [SuFile] instance representing the configuration file.
     */
    fun getConfigFile(id: ModId): SuFile

    /**
     * Provides the override configuration file for a given module ID.
     * This file contains user-specific overrides for the base configuration.
     * It might not exist if no overrides have been set.
     *
     * @param id The [ModId] of the module for which to get the override config file.
     * @return An [SuFile] instance representing the override configuration file, or null if not applicable or defined.
     */
    fun getOverrideConfigFile(id: ModId): SuFile?

    /**
     * Retrieves the class type of the configuration data.
     * This is used by Moshi for deserialization.
     *
     * @return The [Class] of the configuration data type [T].
     */
    fun getConfigType(): Class<T>

    /**
     * Provides a default instance of the configuration.
     * This is used as a fallback if loading or parsing fails.
     *
     * @param id The module ID for which to get the default config.
     * @return A default instance of the configuration.
     */
    fun getDefaultConfigFactory(id: ModId): T

    /**
     * Determines the strategy for merging list values in the configuration.
     * Defaults to [ConfigFileMergeStrategy.REPLACE].
     *
     * @return The [ConfigFileMergeStrategy] to use.
     */
    fun getMergeStrategy(): ConfigFileMergeStrategy = ConfigFileMergeStrategy.REPLACE
}

/**
 * Manages configuration files by layering a base config with a user-specific override,
 * using the SuFile class for potentially privileged file access.
 *
 * This class is thread-safe and now leverages Kotlin's coroutines for reactive state management.
 *
 * The `__module__identifier__` property is reversed for [ModId].
 *
 * @param T The data class type representing the configuration structure.
 */
abstract class ConfigFile<T> : IConfig<T> {
    // A cache to store loaded configurations as reactive StateFlows.
    @Json(ignore = true)
    private val configCache = mutableMapOf<ModId, MutableStateFlow<T>>()

    @Json(ignore = true)
    private val modConfigLocks = ConcurrentHashMap<ModId, Mutex>()

    @Json(ignore = true)
    private val configAdapter: JsonAdapter<T> = moshi.adapter(getConfigType())

    @Json(ignore = true)
    private val mapAdapter: JsonAdapter<Map<String, Any?>> = moshi.adapter(mapType)

    // Using a companion object for static-like members in Kotlin
    companion object {
        private const val TAG = "ConfigFile"

        private val mapType: ParameterizedType =
            Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)

        /**
         * Public utility to deserialize a JSON string into a config object.
         * @param json The JSON string.
         * @param type The class of the target object.
         * @param T The generic type of the object.
         * @return The deserialized object, or null if parsing fails.
         */
        fun <T> fromJson(json: String, type: Class<T>): T? {
            return try {
                Moshi.Builder().build().adapter(type).fromJson(json)
            } catch (_: IOException) {
                // Log the exception if you have a logging framework
                null
            }
        }

        fun MutableMap<String, Any?>.toJson(intents: Int = 2): String {
            return moshi.adapter(Map::class.java).indent(" ".repeat(intents)).toJson(this)
        }
    }

    /**
     * Serializes the current configuration to a JSON string.
     *
     * @param intents The number of spaces to use for indentation in the JSON output. Defaults to 2.
     * @return The JSON string representation of the configuration.
     */
    fun toJson(intents: Int = 2): String {
        return configAdapter.indent(" ".repeat(intents)).toJson(getConfig())
    }

    /**
     * Exposes the configuration for a specific module as a reactive [StateFlow].
     *
     * @return A [StateFlow] of the configuration instance.
     */
    fun getConfigStateFlow(): StateFlow<T> {
        return synchronized(configCache) {
            configCache.getOrPut(getModuleId()) {
                val initialConfig = loadConfigInternal()
                MutableStateFlow(initialConfig)
            }.asStateFlow()
        }
    }

    /**
     * Gets the current configuration snapshot for a specific module ID.
     *
     * @return The current configuration instance.
     * @param disableCache If true, bypasses the cache and reloads the configuration.
     */
    fun getConfig(disableCache: Boolean = false): T {
        val id = getModuleId()

        if (disableCache) {
            return loadConfigInternal(forceNewInstance = true)
        }

        return synchronized(configCache) {
            val flow = configCache.getOrPut(id) {
                val initialConfig = loadConfigInternal()
                MutableStateFlow(initialConfig)
            }
            flow.value
        }
    }

    /**
     * Saves the modified configuration.
     *
     * This function allows for type-safe modification of the configuration data.
     * It merges the changes provided in the `builderAction` with the existing
     * override configuration (or the base configuration if no override file exists).
     *
     * The changes are applied to a mutable map representation of the configuration.
     * The `builderAction` lambda receives a `MutableConfigMap<V>` as its receiver,
     * allowing for direct manipulation of configuration values.
     *
     * The updated configuration is then written to the appropriate file (override or base)
     * in JSON format. Finally, the in-memory cache and the reactive `StateFlow`
     * are updated with the new configuration.
     *
     * This operation is performed asynchronously on an I/O dispatcher and is
     * protected by a mutex to ensure thread safety when multiple modules or
     * parts of the application try to save configuration concurrently for the same module.
     *
     * If no changes are made within the `builderAction` (i.e., the resulting `updates` map is empty),
     * the save operation is skipped.
     *
     * @param V The type of the value being modified in the configuration.
     * @param builderAction A lambda function that defines the modifications to be made
     *                      to the configuration. It takes the current configuration object (`T`)
     *                      as an argument and operates on a `MutableConfigMap<V>` receiver.
     *                      Example: `save<String> { currentConfig -> "some_key" change "new_value" }`
     */
    suspend fun <V : Any?> save(
        builderAction: MutableConfigMap<V>.(T) -> Unit,
    ) {
        val id = getModuleId()
        val overrideConfigFile = getOverrideConfigFile(id)
        val configFile = getConfigFile(id)

        val updates = buildMutableConfig(getConfig(), builderAction)
        if (updates.isEmpty()) return

        val mutex = modConfigLocks.getOrPut(id) { Mutex() }

        mutex.withLock {
            withContext(Dispatchers.IO) {
                // If we have an override file, use it; otherwise create/use config file for overrides
                val targetFile = overrideConfigFile ?: configFile

                // Read existing override content (or empty if file doesn't exist)
                val existingText = if (targetFile.exists()) targetFile.readText() else "{}"
                val existingMap = mapAdapter.fromJson(existingText)?.toMutableMap()
                    ?: mutableMapOf()

                // Merge updates into existing override data
                existingMap.putAll(updates)

                // Ensure parent directory exists
                targetFile.parentFile?.let { parent ->
                    SuFile(parent.absolutePath).mkdirs()
                }

                // Write updated override file
                targetFile.writeText(
                    data = mapAdapter.indent("  ").toJson(existingMap)
                )

                // Load the new configuration and update cache
                val newConfig = loadConfigInternal(forceNewInstance = true)
                synchronized(configCache) {
                    val flow = configCache[id]
                    if (flow != null) {
                        flow.value = newConfig
                    } else {
                        configCache[id] = MutableStateFlow(newConfig)
                    }
                }
            }
        }
    }

    private fun loadConfigInternal(forceNewInstance: Boolean = false): T {
        val id: ModId = getModuleId()
        return try {
            prepareOverrideFile()

            val overrideConfigFile: SuFile? = getOverrideConfigFile(id)
            val configFile: SuFile = getConfigFile(id)

            val baseJson = if (configFile.exists()) configFile.readText() else "{}"
            val overrideJson = if (overrideConfigFile?.exists() == true) {
                overrideConfigFile.readText()
            } else "{}"

            val baseMap = jsonToMap(baseJson).apply { set("__module__identifier__", id) }
            val overrideMap = jsonToMap(overrideJson).apply { set("__module__identifier__", id) }

            val mergedMap = deepMerge(baseMap, overrideMap)
            val jsonMergedMap: String? = mapAdapter.toJson(mergedMap)
            if (jsonMergedMap == null) return getDefaultConfigFactory(id)

            val parsed = configAdapter.fromJson(jsonMergedMap) ?: getDefaultConfigFactory(id)

            val value = if (forceNewInstance) {
                // force a new reference
                configAdapter.fromJson(configAdapter.toJson(parsed))
            } else parsed

            if (value == null) {
                Log.e(TAG, "Failed to parse configuration", Exception("Adapter returned null"))
                return getDefaultConfigFactory(id)
            }

            value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load configuration", e)
            getDefaultConfigFactory(id)
        }
    }

    private fun prepareOverrideFile() {
        val id = getModuleId()
        val overrideConfigFile = getOverrideConfigFile(id)

        // Only prepare the override file if one is configured
        overrideConfigFile.nullable { file ->
            if (!file.exists()) {
                val parentDir = file.parentFile
                parentDir.nullable {
                    SuFile(it.absolutePath).mkdirs()
                }
                file.writeText("{}")
            }
        }
    }

    private fun jsonToMap(json: String?): MutableMap<String, Any?> {
        if (json.isNullOrBlank()) {
            return mutableMapOf()
        }
        return try {
            mapAdapter.fromJson(json)?.toMutableMap() ?: mutableMapOf()
        } catch (_: IOException) {
            mutableMapOf()
        }
    }

    private fun deepMerge(
        base: Map<String, Any?>,
        other: Map<String, Any?>,
    ): MutableMap<String, Any?> {
        val result = base.toMutableMap()
        for ((key, overrideValue) in other) {
            val baseValue = result[key]
            result[key] = when {
                baseValue is Map<*, *> && overrideValue is Map<*, *> -> {
                    deepMerge(baseValue.asStringMap(), overrideValue.asStringMap())
                }

                baseValue is List<*> && overrideValue is List<*> -> {
                    when (getMergeStrategy()) {
                        ConfigFileMergeStrategy.REPLACE -> overrideValue
                        ConfigFileMergeStrategy.APPEND -> baseValue + overrideValue
                        ConfigFileMergeStrategy.DEDUPLICATE -> (baseValue + overrideValue).distinct()
                    }
                }

                overrideValue != null -> overrideValue
                else -> baseValue
            }
        }
        return result
    }


    private fun Any?.asStringMap(): Map<String, Any?> {
        val self = (this as? Map<*, *>)

        if (self == null) {
            return emptyMap()
        }

        val m = self.mapNotNull { (key, value) ->
            (key as? String)?.let { it to value }
        }

        return m.toMap()
    }
}

interface MutableConfig<V> : MutableMap<String, V> {
    infix fun String.change(that: V): V?
    infix fun String.to(that: V): V?
}

inline fun <reified T : Any> T.toMutableConfig(): MutableConfigMap<Any?> {
    val map = MutableConfigMap<Any?>()
    map.putAll(toMap())
    return map
}
