package com.dergoogler.mmrl.datastore.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class RepositoryMenu @OptIn(ExperimentalSerializationApi::class) constructor(
    @ProtoNumber(1) val option: Option = Option.Name,
    @ProtoNumber(2) val descending: Boolean = false,
    @ProtoNumber(3) val pinInstalled: Boolean = true,
    @ProtoNumber(4) val pinUpdatable: Boolean = true,
    @ProtoNumber(5) val showIcon: Boolean = true,
    @ProtoNumber(6) val showLicense: Boolean = true,
    @ProtoNumber(7) val showUpdatedTime: Boolean = false,
    @ProtoNumber(8) val showCover: Boolean = true,
    @ProtoNumber(9) val showVerified: Boolean = true,
    @ProtoNumber(10) val showAntiFeatures: Boolean = true,
    @ProtoNumber(11) val repoListMode: RepoListMode = RepoListMode.Detailed,
    @ProtoNumber(12) val showCategory: Boolean = true,
)