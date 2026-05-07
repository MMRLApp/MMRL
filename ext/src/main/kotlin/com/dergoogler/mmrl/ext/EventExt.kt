package com.dergoogler.mmrl.ext

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay


@Composable
fun eventPainterResource(
    content: @Composable EventPainterScope.() -> Unit
): Painter {
    val scope = remember { EventPainterScope() }
    scope.content()

    val painter = scope.events
        .firstOrNull { it.isActive() }
        ?.painter
        ?: scope.defaultPainter

    return painter
}

class EventPainterScope {
    internal val events = mutableListOf<EventResource>()
    internal lateinit var defaultPainter: Painter

    @Composable
    fun eventResource(
        start: String,
        end: String,
        @DrawableRes res: Int
    ) {
        events.add(
            EventResource(
                start = start,
                end = end,
                painter = painterResource(id = res)
            )
        )
    }

    @Composable
    fun defaultResource(@DrawableRes res: Int) {
        defaultPainter = painterResource(id = res)
    }
}

data class EventResource(
    val start: String,
    val end: String,
    val painter: Painter
) {
    fun isActive(): Boolean = checkDateRange(start, end)
}

// Fixed version of your date checking logic
private fun checkDateRange(
    startDate: String,
    endDate: String,
): Boolean {
    val currentDateTime = LocalDateTime.now()
    val currentMonthDay = MonthDay.from(currentDateTime)
    val currentTime = currentDateTime.toLocalTime()

    val (startMonthDay, startTime) = parseMonthDayTime(startDate)
    val (endMonthDay, endTime) = parseMonthDayTime(endDate)

    // Handle same day events
    if (startMonthDay == endMonthDay) {
        return currentMonthDay == startMonthDay &&
                currentTime >= startTime &&
                currentTime < endTime
    }

    // Handle multi-day events
    return when {
        currentMonthDay == startMonthDay -> currentTime >= startTime
        currentMonthDay == endMonthDay -> currentTime < endTime
        currentMonthDay > startMonthDay && currentMonthDay < endMonthDay -> true
        else -> false
    }
}

private fun parseMonthDayTime(dateTime: String): Pair<MonthDay, LocalTime> {
    val parts = dateTime.split(" ")
    val dateParts = parts[0].split("-")
    val timeParts = parts[1].split(":")

    val monthDay = MonthDay.of(dateParts[1].toInt(), dateParts[0].toInt())
    val time = LocalTime.of(timeParts[0].toInt(), timeParts[1].toInt())

    return monthDay to time
}