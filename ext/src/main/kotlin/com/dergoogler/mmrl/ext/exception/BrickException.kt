package com.dergoogler.mmrl.ext.exception

open class BrickException(
    message: String? = null,
    cause: Throwable? = null,
    val helpMessage: String? = null,
    val helpLink: String? = null,
) : RuntimeException(message, cause)
