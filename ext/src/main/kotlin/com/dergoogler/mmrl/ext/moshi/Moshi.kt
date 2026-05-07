package com.dergoogler.mmrl.ext.moshi

import com.squareup.moshi.Moshi

val moshi: Moshi
    get() =
        Moshi
            .Builder()
            .build()
