package dev.dergoogler.mmrl.compat.model

data class DialogQueueData(
    val title: String,
    val description: String,
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
