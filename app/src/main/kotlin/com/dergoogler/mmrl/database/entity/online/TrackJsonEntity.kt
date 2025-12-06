package com.dergoogler.mmrl.database.entity.online

import androidx.room.Entity
import androidx.room.TypeConverters
import com.dergoogler.mmrl.model.online.TrackJson

@Entity(tableName = "track")
@TypeConverters
data class TrackJsonEntity(
    val type: String,
    val added: Float? = 0f,
    val source: String,
    val antifeatures: List<String>? = null,
) {
    constructor(original: TrackJson) : this(
        type = original.type.name,
        added = original.added,
        source = original.source,
        antifeatures = original.antifeatures,
    )

    fun toTrack() =
        TrackJson(
            typeName = type,
            added = added,
            source = source,
            antifeatures = antifeatures,
        )
}
