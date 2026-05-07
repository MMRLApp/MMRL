package com.dergoogler.mmrl.platform.content

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BulkModule(
    val id: String,
    val name: String,
) : Parcelable {
    companion object
}
