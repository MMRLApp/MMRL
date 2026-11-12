package com.dergoogler.mmrl.repository

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class RepositorySearch(val text: String, val prefix: String, val categoryPattern: String) {
    companion object {
        fun parse(raw: String): RepositorySearch {
            val prefix = listOf("id", "name", "author", "category")
                .firstOrNull { raw.startsWith("$it:", ignoreCase = true) }.orEmpty()
            val text = if (prefix.isEmpty()) raw.trim() else raw.substringAfter(":").trim()
            val category = Json.encodeToString(text)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
            return RepositorySearch(text, prefix, "%$category%")
        }
    }
}
