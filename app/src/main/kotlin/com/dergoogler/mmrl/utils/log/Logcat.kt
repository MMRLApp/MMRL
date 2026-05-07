package com.dergoogler.mmrl.utils.log

import android.content.Context
import com.dergoogler.mmrl.App
import com.dergoogler.mmrl.ext.getLogPath
import com.dergoogler.mmrl.ext.shareFile
import com.dergoogler.mmrl.utils.log.LogText.Companion.toLogPriority

object Logcat {
    const val FILE_NAME = "mmrl_log"

    private val context by lazy { App.context }
    private val uid by lazy { context.applicationInfo.uid }
    private val logFile by lazy { context.getLogPath(FILE_NAME) }

    fun getCurrent(): List<String> =
        try {
            val command =
                arrayOf(
                    "logcat",
                    "-d",
                    "-v",
                    "threadtime",
                    "--uid",
                    uid.toString(),
                )

            val process = Runtime.getRuntime().exec(command)
            val result =
                process.inputStream.use { stream ->
                    stream
                        .reader()
                        .readLines()
                        .filterNot { it.startsWith("------") }
                }

            process.waitFor()
            result
        } catch (e: Exception) {
            emptyList()
        }

    fun readLogs(): List<LogText> =
        if (logFile.exists()) {
            val logs = mutableListOf<LogText>()
            logFile.readLines().forEach { text ->
                runCatching {
                    LogText.parse(text)
                }.onSuccess {
                    logs.add(it)
                }.onFailure {
                    val last = logs.last()
                    val new = last.copy(message = "${last.message}\n$text")
                    logs[logs.size - 1] = new
                }
            }

            logs.toList()
        } else {
            emptyList()
        }

    fun writeLogs(logs: List<LogText>) {
        if (logs.isEmpty()) return

        val texts = logs.joinToString(separator = "\n", postfix = "\n")
        logFile.appendText(texts)
    }

    fun shareLogs(context: Context) {
        context.shareFile(logFile, "text/plain")
    }

    fun List<String>.toLogTextList(): List<LogText> {
        val tmp = map { it.split(": ", limit = 2) }
        val tags = tmp.map { it.first() }.distinct()
        val logs =
            tags.map { tag ->
                val message =
                    tmp
                        .filter {
                            it.first() == tag
                        }.map { it.last() }
                        .reduceOrNull { b, e ->
                            "$b\n$e"
                        } ?: ""

                tag.toLogText().copy(message = message.trim())
            }

        return logs
    }

    private fun String.toLogText(): LogText =
        try {
            split(": ", limit = 2).let { list ->
                val item =
                    list
                        .first()
                        .split(" ")
                        .filter { it != "" }

                LogText(
                    priority = item[4].toLogPriority(),
                    time = "${item[0]} ${item[1]}",
                    process = "${item[2]}-${item[3]}",
                    tag = item[5],
                    message = list.last(),
                )
            }
        } catch (e: Exception) {
            LogText(0, "", "", "", "")
        }
}
