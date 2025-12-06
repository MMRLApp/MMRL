package com.dergoogler.mmrl.utils.log

import android.util.Log

data class LogText(
    val priority: Int,
    val time: String,
    val process: String,
    val tag: String,
    val message: String,
) {
    override fun toString(): String = "[ $time $process ${priority.toTextPriority()}/$tag ] $message"

    companion object {
        fun parse(text: String): LogText {
            val texts = text.split("/", limit = 2)
            val tmp1 =
                texts
                    .first()
                    .replace("[ ", "")
                    .split(" ", limit = 4)
            val tmp2 = texts.last().split(" ] ", limit = 2)

            return LogText(
                priority = tmp1[3].toLogPriority(),
                time = "${tmp1[0]} ${tmp1[1]}",
                process = tmp1[2],
                tag = tmp2[0],
                message = tmp2[1],
            )
        }

        fun String.toLogPriority() =
            when (this) {
                "V" -> Log.VERBOSE
                "D" -> Log.DEBUG
                "I" -> Log.INFO
                "W" -> Log.WARN
                "E" -> Log.ERROR
                else -> 0
            }

        fun Int.toTextPriority() =
            when (this) {
                Log.VERBOSE -> "V"
                Log.DEBUG -> "D"
                Log.INFO -> "I"
                Log.WARN -> "W"
                Log.ERROR -> "E"
                else -> "N"
            }
    }
}
