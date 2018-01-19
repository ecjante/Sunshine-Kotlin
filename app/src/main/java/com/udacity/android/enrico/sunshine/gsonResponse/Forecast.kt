package com.udacity.android.enrico.sunshine.gsonResponse

import android.content.ContentValues
import com.google.gson.annotations.SerializedName
import com.udacity.android.enrico.sunshine.data.WeatherContract
import com.udacity.android.enrico.sunshine.utilities.SunshineDateUtils

/**
 * Created by enrico on 1/18/18.
 */
data class Forecast (
        var date: Long = SunshineDateUtils.getNormalizedUtcDateForToday(),
        @SerializedName(OpenWeatherJsonKeys.OWM_TEMPERATURE_MAIN)
        val temperature: Temp,
        val weather: List<Condition>,
        val wind: Wind
) {
    fun generateContentValues(): ContentValues {
        val weatherValues = ContentValues()
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, date)
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, temperature.humidity)
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, temperature.pressure)
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, wind.speed)
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, wind.direction)
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, temperature.max)
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, temperature.min)
        weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weather[0].id)
        return weatherValues
    }
}

data class Temp (
        @SerializedName(OpenWeatherJsonKeys.OWM_MIN)
        val min: Double,
        @SerializedName(OpenWeatherJsonKeys.OWM_MAX)
        val max: Double,
        val pressure: Double,
        val humidity: Int
)

data class Condition (
        val id: Int
)

data class Wind (
        @SerializedName(OpenWeatherJsonKeys.OWM_WINDSPEED)
        val speed: Double,
        @SerializedName(OpenWeatherJsonKeys.OWM_WIND_DIRECTION)
        val direction: Double
)