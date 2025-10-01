@file:Suppress("PropertyName")

package com.dergoogler.mmrl.platform.model

import android.content.Context
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.file.config.ConfigFile
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleConfigDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.getOrPut
import kotlin.jvm.java

@JsonClass(generateAdapter = true)
data class ModuleConfig(
    val __module__identifier__: ModId,
    @Json(name = "name")
    val nam: Any? = null,
    @Json(name = "description")
    val desc: Any? = null,
    @Json(name = "webui-engine")
    val webuiEngine: Any? = "wx",
    val cover: String? = null,
) : ConfigFile<ModuleConfig>() {
    override fun getModuleId(): ModId = __module__identifier__
    override fun getConfigFile(id: ModId): SuFile = SuFile(id.moduleDir, "config.json")
    override fun getOverrideConfigFile(id: ModId): SuFile? =
        SuFile(id.moduleConfigDir, "config.module.json")

    override fun getConfigType(): Class<ModuleConfig> = ModuleConfig::class.java
    override fun getDefaultConfigFactory(id: ModId): ModuleConfig = ModuleConfig(id)

    val locale: String get() = Locale.getDefault().language

    private fun get(prop: Any?, selector: String, default: String = "en"): String? = try {
        when (prop) {
            is String -> prop
            is Map<*, *> -> prop[selector] as? String ?: prop[default] as? String
            else -> null
        }
    } catch (e: Exception) {
        null
    }

    val description
        get(): String? = get(
            prop = desc,
            selector = locale
        )

    val name
        get(): String? = get(
            prop = nam,
            selector = locale
        )

    fun getWebuiEngine(context: Context): String? = get(
        prop = webuiEngine,
        selector = context.packageName
    )
}

private val moduleConfigCache = ConcurrentHashMap<ModId, ModuleConfig>()

val ModId.ModuleConfig: ConfigFile<ModuleConfig>
    get() = moduleConfigCache.getOrPut(this) { ModuleConfig(this) }


fun ModId.toModuleConfigState(): StateFlow<ModuleConfig> {
    return ModuleConfig.getConfigStateFlow()
}

fun ModId.toModuleConfig(disableCache: Boolean = false): ModuleConfig {
    return ModuleConfig.getConfig(disableCache)
}