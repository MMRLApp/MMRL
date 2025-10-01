package com.dergoogler.mmrl.platform.util

import com.dergoogler.mmrl.platform.file.config.JSONCollectionAdapter
import com.dergoogler.mmrl.platform.file.config.JSONNullAdapter
import com.squareup.moshi.Moshi

val moshi: Moshi by lazy {
    Moshi.Builder()
        .add(JSONCollectionAdapter())
        .add(JSONNullAdapter())
        .build()
}