package com.dergoogler.mmrl.ext

import java.util.Locale

fun Float.toHex(): String =
    (this * 255)
        .toInt()
        .coerceIn(0, 255)
        .toString(16)
        .padStart(2, '0')

fun Int.toDollars(divideBy: Double = 100.0): String = "$%.2f".format(Locale.getDefault(), this / divideBy)
