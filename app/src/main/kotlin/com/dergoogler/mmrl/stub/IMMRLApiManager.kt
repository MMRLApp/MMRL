package com.dergoogler.mmrl.stub

import com.dergoogler.mmrl.model.online.Blacklist
import com.dergoogler.mmrl.model.online.Changelog
import com.dergoogler.mmrl.model.online.Contributor
import com.dergoogler.mmrl.model.online.ExploreRepository
import com.dergoogler.mmrl.model.online.Sponsor
import com.dergoogler.mmrl.network.NetworkUtils
import retrofit2.Call
import retrofit2.create
import retrofit2.http.GET

interface IMMRLApiManager {
    @get:GET("api/changelog.json")
    val changelog: Call<List<Changelog>>

    @get:GET("api/blacklist.json")
    val blacklist: Call<List<Blacklist>>

    @get:GET("api/repositories.json")
    val repositories: Call<List<ExploreRepository>>

    @get:GET("api/sponsors.json")
    val sponsors: Call<List<Sponsor>>

    @get:GET("api/contributors.json")
    val contributors: Call<List<Contributor>>

    companion object {
        fun build(): IMMRLApiManager =
            NetworkUtils
                .createRetrofit()
                .baseUrl("https://mmrl.dev/")
                .build()
                .create()
    }
}
