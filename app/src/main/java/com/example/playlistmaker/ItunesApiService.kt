package com.example.playlistmaker

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.create
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

data class ItunesResponse(
    val resultCount: Int,
    val results: List<Track>
)

interface ItunesApiService {
    @GET("/search?entity=song")
    fun getTracks(
        @Query("term") text: String
    ): Call<ItunesResponse>
}