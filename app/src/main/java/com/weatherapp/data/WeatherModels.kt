package com.weatherapp.data

import retrofit2.http.GET
import retrofit2.http.Query

class WeatherModels {
    data class WeatherResponse(
        val name: String,
        val main: Main,
        val weather: List<Weather>,
        val wind: Wind = Wind(0.0),
        val sys: Sys = Sys(0, 0)
    )
    data class Main(
        val temp: Double,
        val feels_like: Double = temp,
        val humidity: Int,
        val pressure: Int = 0
    )
    data class Weather(val description: String, val icon: String)
    data class Wind(val speed: Double)
    data class Sys(val sunrise: Long, val sunset: Long)

    interface WeatherApi {
        @GET("data/2.5/weather")
        suspend fun getWeather(
            @Query("q") city: String,
            @Query("appid") apiKey: String,
            @Query("units") units: String = "metric",
            @Query("lang") language: String = "ru"
        ): WeatherResponse
    }
}
