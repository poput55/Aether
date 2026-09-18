package com.weatherapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.weatherapp.screens.WeatherState
import com.weatherapp.screens.WeatherViewModel
import com.weatherapp.ui.theme.WeatherAppTheme
import kotlin.math.roundToInt
import kotlin.random.Random

private val Ink = Color(0xFF191B18)
private val Paper = Color(0xFFDDD4BB)
private val Acid = Color(0xFFD4E15A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { WeatherAppTheme { WeatherScreen() } }
    }
}

@Composable
private fun Stamp(text: String, color: Color = Acid) {
    Text(text, color = color, fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 2.sp)
}

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("weather_pref", Context.MODE_PRIVATE) }
    var city by rememberSaveable { mutableStateOf(prefs.getString("savedCity", "") ?: "") }
    var fahrenheit by rememberSaveable { mutableStateOf(prefs.getBoolean("fahrenheit", false)) }
    var favorites by remember { mutableStateOf(prefs.getStringSet("favorites", emptySet())!!.toSet()) }
    val state by viewModel.weatherState.collectAsState()
    fun search(value: String) {
        city = value
        if (value.isNotBlank()) prefs.edit().putString("savedCity", value.trim()).apply()
        viewModel.fetchWeather(value.trim())
    }
    fun degrees(value: Double) = "${(if (fahrenheit) value * 1.8 + 32 else value).roundToInt()}°"

    Box(Modifier.fillMaxSize().background(Ink)) {
        Canvas(Modifier.matchParentSize()) {
            val random = Random(42)
            repeat(2200) {
                drawCircle(Paper.copy(alpha = random.nextFloat() * .12f), random.nextFloat() * 1.4f,
                    Offset(random.nextFloat() * size.width, random.nextFloat() * size.height))
            }
            repeat(18) {
                val y = random.nextFloat() * size.height
                drawLine(Paper.copy(alpha = .06f), Offset(0f, y), Offset(size.width, y - 50f))
            }
        }
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Stamp("ATMOSPHERIC ARCHIVE")
                Text("[ A / 01 ]", color = Paper, fontFamily = FontFamily.Monospace)
            }
            Column {
                Text("AETHER", color = Paper, fontSize = 56.sp, fontWeight = FontWeight.Black, letterSpacing = (-3).sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stamp("НЕБО НЕ ПОДЧИНЯЕТСЯ.")
                    Stamp("EST. 2026", Paper)
                }
            }
            HorizontalDivider(color = Paper.copy(alpha = .4f))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Stamp("01 / НАЙДИ СВОЙ ГОРОД", Paper)
                OutlinedTextField(value = city, onValueChange = { city = it }, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Москва, London, Tokyo…") }, singleLine = true, shape = RectangleShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { search(city) }))
                Button(onClick = { search(city) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Acid, contentColor = Ink), enabled = state !is WeatherState.Loading) {
                    Text("ПОЙМАТЬ СИГНАЛ  ↗", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
            }
            if (favorites.isNotEmpty()) {
                Stamp("ЗАКЛАДКИ / НАЖМИ, ЧТОБЫ ОТКРЫТЬ", Paper)
                favorites.sorted().forEach { favorite ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { search(favorite) }, modifier = Modifier.weight(1f), shape = RectangleShape) { Text(favorite) }
                        TextButton(onClick = {
                            favorites = favorites - favorite
                            prefs.edit().putStringSet("favorites", favorites).apply()
                        }) { Text("Убрать") }
                    }
                }
            }
            when (val current = state) {
                WeatherState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Acid)
                    Stamp("ПРИНИМАЕМ СИГНАЛ…")
                }
                WeatherState.Idle -> Column(Modifier.fillMaxWidth().border(1.dp, Paper.copy(alpha = .3f)).padding(24.dp)) {
                    Text("NO SIGNAL", color = Paper, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Text("У каждого города своё настроение.\nВведи название — узнай его сейчас.", color = Paper.copy(alpha = .7f))
                }
                is WeatherState.Error -> Column(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.error).padding(20.dp)) {
                    Stamp("СИГНАЛ ПОТЕРЯН", MaterialTheme.colorScheme.error)
                    Text(current.message, color = Paper, modifier = Modifier.padding(vertical = 12.dp))
                    OutlinedButton(onClick = { search(city) }, shape = RectangleShape) { Text("Повторить запрос") }
                }
                is WeatherState.Succes -> {
                    val data = current.data
                    Column(Modifier.graphicsLayer { rotationZ = -1f }.fillMaxWidth().background(Paper).padding(24.dp)) {
                        Text("●  WEATHER REPORT / ТЕКУЩАЯ ПОГОДА", color = Ink, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        Spacer(Modifier.height(18.dp))
                        Text(data.name.uppercase(), color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text(degrees(data.main.temp), color = Ink, fontSize = 92.sp, fontWeight = FontWeight.Black, letterSpacing = (-6).sp)
                        Text(data.weather.firstOrNull()?.description?.uppercase() ?: "Нет описания", color = Ink, fontFamily = FontFamily.Monospace)
                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = Ink)
                        Text("ОЩУЩАЕТСЯ КАК ${degrees(data.main.feels_like)}", color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp))
                        Text("|||| ||| || ||||| || ||| |||||", color = Ink, fontSize = 22.sp, letterSpacing = 3.sp)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { search(data.name) }) { Text("↻ Обновить") }
                        TextButton(onClick = {
                            favorites = if (data.name in favorites) favorites - data.name else favorites + data.name
                            prefs.edit().putStringSet("favorites", favorites).apply()
                        }) { Text(if (data.name in favorites) "★ В закладках" else "☆ Сохранить") }
                    }
                    Stamp("02 / ДЕТАЛИ АТМОСФЕРЫ")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Metric("ВЛАЖНОСТЬ", "${data.main.humidity}%", Modifier.weight(1f))
                        Metric("ВЕТЕР", "${data.wind.speed} м/с", Modifier.weight(1f))
                    }
                    Metric("ДАВЛЕНИЕ", "${data.main.pressure} гПа", Modifier.fillMaxWidth())
                }
            }
            HorizontalDivider(color = Paper.copy(alpha = .3f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Stamp("ЕДИНИЦЫ / ${if (fahrenheit) "°F" else "°C"}", Paper)
                Switch(checked = fahrenheit, onCheckedChange = {
                    fahrenheit = it
                    prefs.edit().putBoolean("fahrenheit", it).apply()
                }, thumbContent = { Text(if (fahrenheit) "F" else "C", fontSize = 10.sp, color = Ink) })
            }
            Stamp("OPENWEATHER / CURRENT CONDITIONS", Paper.copy(alpha = .5f))
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Metric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.border(1.dp, Paper.copy(alpha = .3f)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Stamp(label, Paper.copy(alpha = .6f))
        Text(value, color = Acid, fontWeight = FontWeight.Bold, fontSize = 24.sp, fontFamily = FontFamily.Monospace)
    }
}
