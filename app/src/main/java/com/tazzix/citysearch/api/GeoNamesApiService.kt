package com.tazzix.citysearch.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GeoNamesApiService {
    @GET("searchJSON")
    suspend fun searchCities(
        @Query("name_startsWith") nameStartsWith: String,
        @Query("maxRows") maxRows: Int = 10,
        @Query("username") username: String = "keep_truckin"
    ): GeoNamesResponse
}
