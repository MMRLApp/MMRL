package com.dergoogler.mmrl.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.dergoogler.mmrl.datastore.model.UserPreferences
import kotlinx.serialization.SerializationException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class UserPreferencesSerializer
    @Inject
    constructor() : Serializer<UserPreferences> {
        override val defaultValue = UserPreferences()

        override suspend fun readFrom(input: InputStream) =
            try {
                UserPreferences.decodeFrom(input)
            } catch (e: SerializationException) {
                throw CorruptionException("Failed to read proto", e)
            }

        override suspend fun writeTo(
            t: UserPreferences,
            output: OutputStream,
        ) {
            t.encodeTo(output)
        }
    }
