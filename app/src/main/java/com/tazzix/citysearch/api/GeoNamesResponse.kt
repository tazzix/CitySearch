package com.tazzix.citysearch.api

import com.google.gson.annotations.SerializedName

data class GeoNamesResponse(
    @SerializedName("geonames")
    val geonames: List<GeoName> = emptyList(),
    @SerializedName("totalResultsCount")
    val totalResultsCount: Int = 0
)

data class GeoName(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("adminName1")
    val adminName1: String = "", // State/Province
    @SerializedName("countryName")
    val countryName: String = "",
    @SerializedName("countryCode")
    val countryCode: String = "",
    @SerializedName("lat")
    val lat: String = "",
    @SerializedName("lng")
    val lng: String = "",
    @SerializedName("population")
    val population: Long = 0
)
