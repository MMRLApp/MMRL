package com.dergoogler.mmrl.model.online

import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.model.state.OnlineState

data class OtherSources(
    val repo: Repo,
    val online: OnlineModule,
    val state: OnlineState,
) {
    companion object {
        val example =
            OtherSources(
                repo = Repo.example(),
                online = OnlineModule.example(),
                state = OnlineState.example(),
            )
    }
}
