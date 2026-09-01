package com.weatherapp.data

import retrofit2.http.GET
import retrofit2.http.Query

class WeatherModels {
    data class WeatherResponse(
        val name: String,
        val main: Main,
        val weather:List<Weather>
    )
    data class Main(
        val temp: Double,
        val humidity: Int
    )
    data class Weather(
        val description: String,
        val icon: String
    )

    interface WeatherApi {
        @GET("data/2.5/weather")
        suspend fun getWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric"
        ): WeatherResponse
    }
}