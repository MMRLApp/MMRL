@file:Suppress("unused", "REDUNDANT_ELSE_IN_WHEN")

package com.dergoogler.mmrl.platform.file.config

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types

sealed interface JSONCollection

@JsonClass(generateAdapter = true)
data class JSONString(@Json(name = "value") val string: String) : JSONCollection {
    companion object {
        val EMPTY = JSONString("")
        fun String.toJsonString() = JSONString(this)
    }
}

@JsonClass(generateAdapter = true)
data class JSONBoolean(@Json(name = "value") val boolean: Boolean) : JSONCollection {
    companion object {
        val TRUE = JSONBoolean(true)
        val FALSE = JSONBoolean(false)
        fun Boolean.toJsonBoolean() = JSONBoolean(this)
    }
}

@JsonClass(generateAdapter = true)
data class JSONArray(@Json(name = "values") val array: List<Any?>) : JSONCollection {
    companion object {
        val EMPTY = JSONArray(emptyList())
        fun List<Any?>.toJsonArray() = JSONArray(this)

        // Helper methods for type-safe access
        fun <T> JSONArray.asListOf(type: Class<T>): List<T>? {
            return array.filterIsInstance(type)
        }

        inline fun <reified T> JSONArray.asListOf(): List<T> {
            return array.filterIsInstance<T>()
        }
    }
}

@JsonClass(generateAdapter = true)
data class JSONNumber(@Json(name = "value") val number: Number) : JSONCollection {
    companion object {
        val ZERO = JSONNumber(0)
        fun Int.toJsonNumber() = JSONNumber(this)
        fun Double.toJsonNumber() = JSONNumber(this)
        fun Float.toJsonNumber() = JSONNumber(this)
        fun Long.toJsonNumber() = JSONNumber(this)
    }

    // Helper properties for easy access
    val intValue: Int get() = number.toInt()
    val longValue: Long get() = number.toLong()
    val doubleValue: Double get() = number.toDouble()
    val floatValue: Float get() = number.toFloat()
}

@JsonClass(generateAdapter = true)
data class JSONObject(@Json(name = "properties") val properties: Map<String, Any?>) :
    JSONCollection {
    companion object {
        val EMPTY = JSONObject(emptyMap())
        fun Map<String, Any?>.toJsonObject() = JSONObject(this)
    }

    // Helper methods for easy access
    operator fun get(key: String): Any? = properties[key]
    fun getString(key: String): String? = properties[key] as? String
    fun getNumber(key: String): Number? = properties[key] as? Number
    fun getBoolean(key: String): Boolean? = properties[key] as? Boolean
    fun getArray(key: String): JSONArray? = properties[key] as? JSONArray
    fun getObject(key: String): JSONObject? = properties[key] as? JSONObject
}

object JSONNull : JSONCollection {
    override fun toString(): String = "JSONNull"
}

val moshi: Moshi by lazy {
    Moshi.Builder()
        .add(JSONCollectionAdapter())
        .add(JSONNullAdapter())
        .build()
}

class JSONCollectionAdapter {
    private val mapAdapter by lazy {
        moshi.adapter<Map<String, Any?>>(
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
        )
    }

    @FromJson
    fun fromJson(reader: JsonReader): JSONCollection? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> JSONString(reader.nextString())
            JsonReader.Token.BOOLEAN -> JSONBoolean(reader.nextBoolean())
            JsonReader.Token.NUMBER -> parseJSONNumber(reader)
            JsonReader.Token.BEGIN_ARRAY -> parseJSONArray(reader)
            JsonReader.Token.BEGIN_OBJECT -> parseJSONObject(reader)
            JsonReader.Token.NULL -> {
                reader.nextNull<Any>()
                JSONNull
            }

            else -> throw JsonDataException("Unexpected token: ${reader.peek()}")
        }
    }

    private fun parseJSONNumber(reader: JsonReader): JSONNumber {
        // Use nextDouble() for all numbers to avoid precision issues
        return JSONNumber(reader.nextDouble())
    }

    private fun parseJSONArray(reader: JsonReader): JSONArray {
        val list = mutableListOf<Any?>()
        reader.beginArray()
        while (reader.hasNext()) {
            list.add(parseDynamicValue(reader))
        }
        reader.endArray()
        return JSONArray(list)
    }

    private fun parseJSONObject(reader: JsonReader): JSONObject {
        val map = mutableMapOf<String, Any?>()
        reader.beginObject()
        while (reader.hasNext()) {
            val key = reader.nextName()
            map[key] = parseDynamicValue(reader)
        }
        reader.endObject()
        return JSONObject(map)
    }

    private fun parseDynamicValue(reader: JsonReader): Any? {
        return when (reader.peek()) {
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NUMBER -> reader.nextDouble()
            JsonReader.Token.BEGIN_ARRAY -> {
                reader.beginArray()
                val list = mutableListOf<Any?>()
                while (reader.hasNext()) {
                    list.add(parseDynamicValue(reader))
                }
                reader.endArray()
                list
            }

            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                val map = mutableMapOf<String, Any?>()
                while (reader.hasNext()) {
                    map[reader.nextName()] = parseDynamicValue(reader)
                }
                reader.endObject()
                map
            }

            JsonReader.Token.NULL -> {
                reader.nextNull<Any>()
                null
            }

            else -> throw JsonDataException("Unsupported token: ${reader.peek()}")
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: JSONCollection?) {
        when (value) {
            is JSONString -> writer.value(value.string)
            is JSONBoolean -> writer.value(value.boolean)
            is JSONNumber -> writer.value(value.number.toDouble())
            is JSONArray -> writeArray(writer, value.array)
            is JSONObject -> writeObject(writer, value.properties)
            is JSONNull -> writer.nullValue()
            null -> writer.nullValue()
            else -> throw JsonDataException("Unknown JSONCollection type: ${value::class.java}")
        }
    }

    private fun writeArray(writer: JsonWriter, array: List<*>) {
        writer.beginArray()
        array.forEach { writeDynamicValue(writer, it) }
        writer.endArray()
    }

    private fun writeObject(writer: JsonWriter, properties: Map<String, Any?>) {
        writer.beginObject()
        properties.forEach { (key, value) ->
            writer.name(key)
            writeDynamicValue(writer, value)
        }
        writer.endObject()
    }

    private fun writeDynamicValue(writer: JsonWriter, value: Any?) {
        when (value) {
            is String -> writer.value(value)
            is Number -> writer.value(value.toDouble())
            is Boolean -> writer.value(value)
            is List<*> -> writeArray(writer, value)
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                writeObject(writer, value as Map<String, Any?>)
            }

            null -> writer.nullValue()
            is JSONCollection -> toJson(writer, value) // Handle nested JSONCollection objects
            else -> {
                // Fallback: convert to string
                writer.value(value.toString())
            }
        }
    }
}

class JSONNullAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): JSONNull? {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Any>()
            return JSONNull
        }
        return null
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: JSONNull?) {
        writer.nullValue()
    }
}

// Extension functions for easier usage
inline fun <reified T> JSONArray.mapElements(transform: (T) -> Any?): JSONArray {
    return JSONArray(array.map { if (it is T) transform(it) else it })
}

inline fun <reified T> JSONArray.toTypedList(): List<T> = array.filterIsInstance<T>()
inline fun <reified T> JSONArray.toTypedMutableList(): MutableList<T> =
    toTypedList<T>().toMutableList()

inline fun <reified T> JSONArray.toTypedMutableStateList(): SnapshotStateList<T> =
    toTypedList<T>().toMutableStateList()