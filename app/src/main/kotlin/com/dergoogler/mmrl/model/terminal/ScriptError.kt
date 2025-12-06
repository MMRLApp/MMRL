package com.dergoogler.mmrl.model.terminal

data class ScriptError(
    val filePath: String,
    val lineNumber: Int,
    val title: String,
    val message: String,
) {
    companion object {
        private val regex = Regex("""^(.*): line (\d+): (.+): (.+)$""")

        fun fromString(input: String): ScriptError? {
            val match = regex.matchEntire(input) ?: return null
            val (filePath, lineStr, errorTitle, errorMessage) = match.destructured
            return ScriptError(
                filePath = filePath,
                lineNumber = lineStr.toInt(),
                title = errorTitle,
                message = errorMessage,
            )
        }
    }
}
