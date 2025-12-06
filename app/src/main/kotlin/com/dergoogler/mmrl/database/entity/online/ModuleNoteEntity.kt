package com.dergoogler.mmrl.database.entity.online

import androidx.room.Entity
import androidx.room.TypeConverters
import com.dergoogler.mmrl.model.online.ModuleNote

@Entity(tableName = "note")
@TypeConverters
data class ModuleNoteEntity(
    val title: String? = null,
    val message: String? = null,
) {
    constructor(original: ModuleNote?) : this(
        title = original?.title,
        message = original?.message,
    )

    fun toNote() =
        ModuleNote(
            title = title,
            message = message,
        )
}
