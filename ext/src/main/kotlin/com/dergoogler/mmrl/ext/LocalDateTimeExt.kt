package com.dergoogler.mmrl.ext

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

fun Float.toDateTime(): String {
    val instant = Instant.fromEpochMilliseconds(times(1000).toLong())
    return instant.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
}

fun Float.toDate(): String {
    val instant = Instant.fromEpochMilliseconds(times(1000).toLong())
    return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
}

fun Long.toDateTime(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    return instant.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
}

fun Long.toDate(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
}

fun Float.toFormattedDate(formatter: DateTimeFormatter? = null): String {
    val instant = Instant.fromEpochMilliseconds((this * 1000).toLong())
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return formatDate(localDateTime, formatter)
}

fun Long.toFormattedDate(formatter: DateTimeFormatter? = null): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return formatDate(localDateTime, formatter)
}

private fun formatDate(
    localDateTime: LocalDateTime,
    formatter: DateTimeFormatter?,
): String =
    if (formatter != null) {
        localDateTime.toJavaLocalDateTime().format(formatter)
    } else {
        val month =
            localDateTime.month.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        val day = localDateTime.dayOfMonth
        val year = localDateTime.year
        "$month $day, $year"
    }

fun Float.toFormattedDateSafely(pattern: String? = null): String =
    try {
        val formatter = pattern?.let { DateTimeFormatter.ofPattern(it) }
        this.toFormattedDate(formatter)
    } catch (e: IllegalArgumentException) {
        this.toFormattedDate()
    }

fun Long.toFormattedDateSafely(pattern: String? = null): String =
    try {
        val formatter = pattern?.let { DateTimeFormatter.ofPattern(it) }
        this.toFormattedDate(formatter)
    } catch (e: IllegalArgumentException) {
        "Invalid date format pattern"
    }

private fun formatDate(localDateTime: LocalDateTime): String {
    val month =
        localDateTime.month.name
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    val day = localDateTime.dayOfMonth
    val year = localDateTime.year
    return "$month $day, $year"
}

fun LocalDateTime.Companion.now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
