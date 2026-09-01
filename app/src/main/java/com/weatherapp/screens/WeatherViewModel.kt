package com.weatherapp.screens

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weatherapp.data.RetrofitClient
import com.weatherapp.data.WeatherModels
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


sealed class WeatherState{
    object Loading: WeatherState()
    data class Succes(val data: WeatherModels.WeatherResponse) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

class WeatherViewModel : ViewModel() {
    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState : StateFlow<WeatherState> = _weatherState
    private val API_KEY = "7c72e7dfc0cc89d5397308b3e6f8d3c9"
    fun fetchWeather(city: String)
    {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading
            try {
                val response = RetrofitClient.weatherApi.getWeather(city,API_KEY)
                _weatherState.value = WeatherState.Succes(response)
            }catch (e: Exception)
            {
                _weatherState.value = WeatherState.Error("Error:%{e.message}")
            }
        }
    }
}