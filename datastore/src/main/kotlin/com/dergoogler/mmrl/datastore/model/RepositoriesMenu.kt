package com.dergoogler.mmrl.datastore.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class RepositoriesMenu
    @OptIn(ExperimentalSerializationApi::class)
    constructor(
        @ProtoNumber(1) val option: Option = Option.Name,
        @ProtoNumber(2) val descending: Boolean = false,
        @ProtoNumber(3) val showModulesCount: Boolean = true,
        @ProtoNumber(4) val showUpdatedTime: Boolean = true,
        @ProtoNumber(5) val showCover: Boolean = true,
    )
