package org.acme


data class WeatherNotifyRequest(
    val weatherForecast: ForecastItem,
    val city: CityInfo
)

data class MainInfo(
    val temp: Double,
    val feels_like: Double,
    val pressure: Double,
    val grnd_level: Double,
    val humidity: Int
)
data class WeatherInfo(
    val main: String,
    val description: String
)
data class ForecastItem(
    val dt: Long,
    val main: MainInfo,
    val weather: List<WeatherInfo>,
    val dt_txt: String?
)
data class CityInfo(var name: String)

