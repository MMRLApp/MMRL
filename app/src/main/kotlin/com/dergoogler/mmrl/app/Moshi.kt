package com.dergoogler.mmrl.app

import com.squareup.moshi.Moshi

val moshi: Moshi
    get() =
        Moshi
            .Builder()
            .build()
