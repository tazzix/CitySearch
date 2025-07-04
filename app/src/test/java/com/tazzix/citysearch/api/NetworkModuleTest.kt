package com.tazzix.citysearch.api

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

class NetworkModuleTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: GeoNamesApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Create a test instance of the API service that points to the mock server
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeoNamesApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test searchCities returns successful response`() = runBlocking {
        // Prepare mock response
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                {
                    "totalResultsCount": 2,
                    "geonames": [
                        {
                            "name": "San Francisco",
                            "adminName1": "California",
                            "countryName": "United States",
                            "countryCode": "US",
                            "lat": "37.77493",
                            "lng": "-122.41942",
                            "population": 864816
                        },
                        {
                            "name": "San Francisco",
                            "adminName1": "Cordoba",
                            "countryName": "Argentina",
                            "countryCode": "AR",
                            "lat": "-31.42797",
                            "lng": "-62.08544",
                            "population": 61260
                        }
                    ]
                }
            """.trimIndent())

        // Queue the mock response
        mockWebServer.enqueue(mockResponse)

        // Make the API call
        val response = apiService.searchCities("san francisco")

        // Verify the request was made correctly
        val request = mockWebServer.takeRequest()
        assertEquals("/searchJSON?name_startsWith=san%20francisco&maxRows=10&username=keep_truckin", request.path)

        // Verify the response was parsed correctly
        assertEquals(2, response.totalResultsCount)
        assertEquals(2, response.geonames.size)
        
        // Verify first city
        val firstCity = response.geonames[0]
        assertEquals("San Francisco", firstCity.name)
        assertEquals("California", firstCity.adminName1)
        assertEquals("United States", firstCity.countryName)
        assertEquals("US", firstCity.countryCode)
        
        // Verify second city
        val secondCity = response.geonames[1]
        assertEquals("San Francisco", secondCity.name)
        assertEquals("Cordoba", secondCity.adminName1)
        assertEquals("Argentina", secondCity.countryName)
        assertEquals("AR", secondCity.countryCode)
    }

    @Test
    fun `test searchCities with empty response`() = runBlocking {
        // Prepare mock response with empty results
        val mockResponse = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("""
                {
                    "totalResultsCount": 0,
                    "geonames": []
                }
            """.trimIndent())

        // Queue the mock response
        mockWebServer.enqueue(mockResponse)

        // Make the API call
        val response = apiService.searchCities("nonexistentcity")

        // Verify the request was made correctly
        val request = mockWebServer.takeRequest()
        assertEquals("/searchJSON?name_startsWith=nonexistentcity&maxRows=10&username=keep_truckin", request.path)

        // Verify the response was parsed correctly
        assertEquals(0, response.totalResultsCount)
        assertEquals(0, response.geonames.size)
    }

    @Test
    fun `test NetworkModule creates valid API service`() {
        // Test that the NetworkModule creates a valid API service instance
        val apiService = NetworkModule.geoNamesApiService
        assertNotNull(apiService)
    }
}
