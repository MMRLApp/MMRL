package com.dergoogler.mmrl.database.entity.online

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dergoogler.mmrl.model.online.Blacklist
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "blacklist")
data class BlacklistEntity(
    @Json(name = "id") @PrimaryKey val blId: String,
    @Json(name = "source") val blSource: String,
    @Json(name = "notes") val blNotes: String? = null,
    @TypeConverters @Json(name = "antifeatures")
    val blAntiFeatures: List<String>? = null,
) {
    constructor(original: Blacklist) : this(
        blId = original.id,
        blSource = original.source,
        blNotes = original.notes,
        blAntiFeatures = original.antifeatures,
    )

    fun toBlacklist() =
        Blacklist(
            id = blId,
            source = blSource,
            notes = blNotes,
            antifeatures = blAntiFeatures,
        )
}
