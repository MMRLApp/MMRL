package dev.dergoogler.mmrl.compat.core

open class BrickException(
    message: String? = null,
    cause: Throwable? = null,
    val helpMessage: String? = null,
    val helpLink: String? = null,
) : RuntimeException(message, cause)
