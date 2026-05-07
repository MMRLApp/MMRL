package com.dergoogler.mmrl.platform.model

data class ShellResult(
    val isSuccess: Boolean,
    val out: List<String>,
    val err: List<String>,
    val exitCode: Int,
)
