package com.dergoogler.mmrl.platform.util

import android.util.Log
import com.dergoogler.mmrl.platform.model.ShellResult

object Shell {
    private const val TAG = "Shell"

    fun String.exec(): Result<String> =
        runCatching {
            Log.d(TAG, "exec: $this")
            val process = ProcessBuilder("sh", "-c", this).start()
            val output =
                process.inputStream
                    .bufferedReader()
                    .readText()
                    .removeSurrounding("", "\n")

            val error =
                process.errorStream
                    .bufferedReader()
                    .readText()
                    .removeSurrounding("", "\n")

            require(process.waitFor().ok()) { error }
            Log.d(TAG, "output: $output")

            output
        }.onFailure {
            Log.e(TAG, Log.getStackTraceString(it))
        }

    fun String.exec(
        stdout: (String) -> Unit,
        stderr: (String) -> Unit,
    ) = runCatching {
        Log.d(TAG, "exec: $this")
        val process = ProcessBuilder("sh", "-c", this).start()
        val output = process.inputStream.bufferedReader()
        val error = process.errorStream.bufferedReader()

        output.forEachLine {
            Log.d(TAG, "output: $it")
            stdout(it)
        }

        error.forEachLine {
            Log.d(TAG, "error: $it")
            stderr(it)
        }

        require(process.waitFor().ok())
    }.onFailure {
        Log.e(TAG, Log.getStackTraceString(it))
    }

    fun Int.ok() = this == 0

    fun String.submit(callback: ShellResult.() -> Unit) {
        Thread {
            val result =
                runCatching {
                    Log.d(TAG, "submit: $this")
                    val process = ProcessBuilder("sh", "-c", this).start()
                    val output = process.inputStream.bufferedReader().readLines()
                    val error = process.errorStream.bufferedReader().readLines()

                    val exitCode = process.waitFor()

                    ShellResult(
                        isSuccess = exitCode == 0,
                        out = output,
                        err = error,
                        exitCode = exitCode,
                    )
                }.getOrElse {
                    ShellResult(
                        isSuccess = false,
                        out = emptyList(),
                        err = listOf(it.message ?: "Unknown error"),
                        exitCode = -1,
                    )
                }

            callback(result)
        }.start()
    }
}
