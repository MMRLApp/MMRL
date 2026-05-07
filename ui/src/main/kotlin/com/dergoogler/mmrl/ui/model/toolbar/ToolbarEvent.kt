package com.dergoogler.mmrl.ui.model.toolbar

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ext.nullable
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay

/**
 * Represents an event displayed in the toolbar.
 *
 * This data class encapsulates the details of an event, including its start and end date/time,
 * an optional title, and an optional icon. It also provides functionality to determine if the
 * event is currently active based on the current date and time.
 *
 * @property start The start date and time of the event in the format "dd-MM HH:mm".
 *                  e.g., "15-08 10:00" for August 15th at 10:00 AM.
 * @property end The end date and time of the event in the format "dd-MM HH:mm".
 *                  e.g., "20-08 17:00" for August 20th at 5:00 PM.
 * @property title An optional title for the event. It can be a String or an Int representing a string resource ID.
 *                 If null, no title will be displayed.
 * @property icon The drawable resource ID for the event's icon. Defaults to `com.dergoogler.mmrl.ui.R.drawable.mmrl_logo`.
 * @property isActive A boolean indicating whether the event is currently active based on the current date and time.
 *                     This value is calculated when the object is created.
 * @property getTitle A composable function that returns the event's title as a String.
 *                    If the title is a string resource ID, it will resolve it to the actual string using `stringResource`.
 *                    If the title is null, it will return null.
 *
 * @constructor Creates a ToolbarEvent instance.
 *
 * @see stringResource
 * @see checkDateRange
 * @see parseMonthDayTime
 */
data class ToolbarEvent(
    val start: String,
    val end: String,
    val title: Any? = null,
    @DrawableRes val icon: Int = com.dergoogler.mmrl.ui.R.drawable.mmrl_logo,
) {
    private fun checkDateRange(
        startDate: String,
        endDate: String,
    ): Boolean {
        val currentDateTime = LocalDateTime.now()
        val currentMonthDay = MonthDay.from(currentDateTime)
        val currentTime = currentDateTime.toLocalTime()

        val (startMonthDay, startTime) = parseMonthDayTime(startDate)
        val (endMonthDay, endTime) = parseMonthDayTime(endDate)

        val isInDateRange =
            currentMonthDay.isAfter(startMonthDay) &&
                currentMonthDay.isBefore(endMonthDay) ||
                currentMonthDay == startMonthDay &&
                currentTime.isAfter(startTime) ||
                currentMonthDay == endMonthDay &&
                currentTime.isBefore(endTime)

        return isInDateRange
    }

    private fun parseMonthDayTime(dateString: String): Pair<MonthDay, LocalTime> {
        val (monthDayPart, timePart) = dateString.split(" ")
        val (day, month) = monthDayPart.split("-").map { it.toInt() }
        val monthDay = MonthDay.of(month, day)
        val time = LocalTime.parse(timePart)
        return monthDay to time
    }

    @Composable
    fun getTitle(): String? =
        title.nullable {
            when (it) {
                is Int -> stringResource(it)
                is String -> it
                else -> null
            }
        }

    val isActive = checkDateRange(start, end)
}
