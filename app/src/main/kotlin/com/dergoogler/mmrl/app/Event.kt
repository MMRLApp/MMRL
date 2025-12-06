package com.dergoogler.mmrl.app

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay

enum class Event {
    NON,
    LOADING,
    SUCCEEDED,
    FAILED,
    ;

    companion object {
        val Event.isNon get() = this == NON
        val Event.isLoading get() = this == LOADING
        val Event.isSucceeded get() = this == SUCCEEDED
        val Event.isFailed get() = this == FAILED
        val Event.isFinished get() = isSucceeded || isFailed
        val Event.isNotReady get() = isNon || isFailed

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

        val isHalloween
            get() =
                checkDateRange(
                    startDate = "31-10 00:00",
                    endDate = "01-11 00:00",
                )

        val isMMRLBirthday
            get() =
                checkDateRange(
                    startDate = "25-04 00:00",
                    endDate = "26-04 00:00",
                )

        val isChristmas
            get() =
                checkDateRange(
                    startDate = "01-12 00:00",
                    endDate = "27-12 00:00",
                )

        val isNewYearsEve
            get() =
                checkDateRange(
                    startDate = "31-12 14:00",
                    endDate = "01-01 14:00",
                )

        val isSomeRandomDates
            get() =
                checkDateRange(
                    startDate = "28-10 00:00",
                    endDate = "29-10 00:00",
                ) ||
                    checkDateRange(
                        startDate = "05-10 00:00",
                        endDate = "06-10 00:00",
                    ) ||
                    checkDateRange(
                        startDate = "18-11 00:00",
                        endDate = "19-11 00:00",
                    ) ||
                    checkDateRange(
                        startDate = "20-11 00:00",
                        endDate = "21-11 00:00",
                    )
    }
}
