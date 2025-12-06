package dev.dergoogler.mmrl.compat

import androidx.room.TypeConverter
import com.dergoogler.mmrl.model.online.ModuleManager
import com.dergoogler.mmrl.model.online.ModuleManagerSolution
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class Converters {
    private val moshi: Moshi =
        Moshi
            .Builder()
            .build()

    private val managerEntityAdapter: JsonAdapter<ModuleManager> = moshi.adapter(ModuleManager::class.java)
    private val rootManagerEntityAdapter: JsonAdapter<ModuleManagerSolution> = moshi.adapter(ModuleManagerSolution::class.java)

    private val stringListAdapter: JsonAdapter<List<String>> =
        moshi.adapter(
            Types.newParameterizedType(List::class.java, String::class.java),
        )

    @TypeConverter
    fun fromManager(entity: ModuleManager?): String? = entity?.let { managerEntityAdapter.toJson(it) }

    @TypeConverter
    fun toManager(data: String?): ModuleManager? = data?.let { managerEntityAdapter.fromJson(it) }

    @TypeConverter
    fun fromRootManager(entity: ModuleManagerSolution?): String? = entity?.let { rootManagerEntityAdapter.toJson(it) }

    @TypeConverter
    fun toRootManager(data: String?): ModuleManagerSolution? = data?.let { rootManagerEntityAdapter.fromJson(it) }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.let { stringListAdapter.toJson(it) }

    @TypeConverter
    fun toStringList(data: String?): List<String>? = data?.let { stringListAdapter.fromJson(it) }
}
