package com.dergoogler.mmrl.platform.content

import android.os.Parcel
import android.os.Parcelable

data class NullableBoolean(
    val value: Boolean?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        when (parcel.readByte()) {
            1.toByte() -> true
            0.toByte() -> false
            else -> null
        },
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeByte(
            when (value) {
                true -> 1
                false -> 0
                null -> -1
            },
        )
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NullableBoolean> {
        override fun createFromParcel(parcel: Parcel): NullableBoolean = NullableBoolean(parcel)

        override fun newArray(size: Int): Array<NullableBoolean?> = arrayOfNulls(size)
    }
}
