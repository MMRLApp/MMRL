package com.dergoogler.mmrl.platform.file

import kotlin.math.log
import kotlin.math.pow

enum class SuFileSizeRepresentation(
    val base: Int,
) {
    Binary(1024),
    Decimal(1000),
    ;

    companion object {
        fun getRepresentation(storageSizeInBytes: Double): SuFileSizeRepresentation =
            if (log(storageSizeInBytes / (1024.0.pow(3)), 2.0) % 1.0 == 0.0) {
                Binary
            } else {
                Decimal
            }
    }
}
