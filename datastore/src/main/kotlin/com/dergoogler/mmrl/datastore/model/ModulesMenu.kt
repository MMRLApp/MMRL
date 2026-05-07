package com.dergoogler.mmrl.datastore.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class ModulesMenu
    @OptIn(ExperimentalSerializationApi::class)
    constructor(
        @ProtoNumber(1) val option: Option = Option.Name,
        @ProtoNumber(2) val descending: Boolean = false,
        @ProtoNumber(3) val pinEnabled: Boolean = true,
        @ProtoNumber(4) val pinAction: Boolean = false,
        @ProtoNumber(5) val pinWebUI: Boolean = false,
        @ProtoNumber(6) val showUpdatedTime: Boolean = true,
        @ProtoNumber(7) val showCover: Boolean = true,
    )
