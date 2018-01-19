package com.udacity.android.enrico.sunshine.data

import android.content.Context
import android.database.Cursor
import com.udacity.android.enrico.sunshine.DetailActivity
import com.udacity.android.enrico.sunshine.R
import com.udacity.android.enrico.sunshine.utilities.SunshineDateUtils
import com.udacity.android.enrico.sunshine.utilities.SunshineWeatherUtils

/**
 * Created by enrico on 1/18/18.
 *
 * Class created to support DetailActivity data binding
 */
class WeatherUiData(context: Context, cursor: Cursor) {
    val weatherImageId: Int
    val dateText: String
    val description: String
    val descriptionA11y: String
    val highString: String
    val highA11y: String
    val lowString: String
    val lowA11y: String
    val humidityString: String
    val humidityA11y: String
    val windString: String
    val windA11y: String
    val pressureString: String
    val pressureA11y: String

    init {
        val weatherId = cursor.getInt(DetailActivity.INDEX_WEATHER_CONDITION_ID)
        weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId)

        val localDateMidnightGmt = cursor.getLong(DetailActivity.INDEX_WEATHER_DATE)
        dateText = SunshineDateUtils.getFriendlyDateString(context, localDateMidnightGmt, true)

        /* Use the weatherId to obtain the proper description */
        description = SunshineWeatherUtils.getStringForWeatherCondition(context, weatherId)

        /* Create the accessibility (a11y) String from the weather description */
        descriptionA11y = context.getString(R.string.a11y_forecast, description)

        /* Read high temperature from the cursor (in degrees celsius) */
        val highInCelsius = cursor.getDouble(DetailActivity.INDEX_WEATHER_MAX_TEMP)
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        highString = SunshineWeatherUtils.formatTemperature(context, highInCelsius)

        /* Create the accessibility (a11y) String from the weather description */
        highA11y = context.getString(R.string.a11y_high_temp, highString)

        /* Read low temperature from the cursor (in degrees celsius) */
        val lowInCelsius = cursor.getDouble(DetailActivity.INDEX_WEATHER_MIN_TEMP)
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        lowString = SunshineWeatherUtils.formatTemperature(context, lowInCelsius)

        lowA11y = context.getString(R.string.a11y_low_temp, lowString)

        /* Read humidity from the cursor */
        val humidity = cursor.getFloat(DetailActivity.INDEX_WEATHER_HUMIDITY)
        humidityString = context.getString(R.string.format_humidity, humidity)

        humidityA11y = context.getString(R.string.a11y_humidity, humidityString)

        /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
        val windSpeed = cursor.getFloat(DetailActivity.INDEX_WEATHER_WIND_SPEED)
        val windDirection = cursor.getFloat(DetailActivity.INDEX_WEATHER_DEGREES)
        windString = SunshineWeatherUtils.getFormattedWind(context, windSpeed, windDirection)

        windA11y = context.getString(R.string.a11y_wind, windString)

        /* Read pressure from the cursor */
        val pressure = cursor.getFloat(DetailActivity.INDEX_WEATHER_PRESSURE)

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        pressureString = context.getString(R.string.format_pressure, pressure)

        pressureA11y = context.getString(R.string.a11y_pressure, pressureString)
    }
}