package com.dergoogler.mmrl.platform.model

import android.content.ServiceConnection

interface IProvider {
    val name: String

    fun isAvailable(): Boolean

    suspend fun isAuthorized(): Boolean

    fun bind(connection: ServiceConnection)

    fun unbind(connection: ServiceConnection)
}
