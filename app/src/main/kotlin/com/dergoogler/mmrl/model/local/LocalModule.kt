package com.dergoogler.mmrl.model.local

import com.dergoogler.mmrl.platform.model.ModId.Companion.toModId
import com.dergoogler.mmrl.utils.Utils

typealias LocalModule = com.dergoogler.mmrl.platform.content.LocalModule

val com.dergoogler.mmrl.platform.content.LocalModule.versionDisplay get() = Utils.getVersionDisplay(version, versionCode)

fun com.dergoogler.mmrl.platform.content.LocalModule.Companion.example() =
    com.dergoogler.mmrl.platform.content.LocalModule(
        id = "local_example".toModId(),
        name = "Example",
        version = "2022.08.16",
        versionCode = 1703,
        author = "Sanmer",
        description = "This is an example!",
        updateJson = "",
        state = State.ENABLE,
        size = 0,
        lastUpdated = 0L,
    )
