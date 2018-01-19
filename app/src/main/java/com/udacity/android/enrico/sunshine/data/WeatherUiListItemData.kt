package com.udacity.android.enrico.sunshine.data

import android.content.Context
import android.database.Cursor
import com.udacity.android.enrico.sunshine.MainActivity
import com.udacity.android.enrico.sunshine.R
import com.udacity.android.enrico.sunshine.utilities.SunshineDateUtils
import com.udacity.android.enrico.sunshine.utilities.SunshineWeatherUtils

/**
 * Created by enrico on 1/18/18.
 *
 * Class created to support forecast list item data binding.
 */
class WeatherUiListItemData {
    val weatherImageId: Int
    val dateString: String
    val description: String
    val descriptionA11y: String
    val highString: String
    val highA11y: String
    val lowString: String
    val lowA11y: String

    constructor(context: Context, cursor: Cursor, weatherImageId: Int) {
        /****************
         * Weather Icon *
         */
        val weatherId = cursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID)
        this.weatherImageId = weatherImageId

        /****************
         * Weather Date *
         */
        /* Read date from the cursor */
        val dateInMillis = cursor.getLong(MainActivity.INDEX_WEATHER_DATE)
        /* Get human readable string using our utility method */
        dateString = SunshineDateUtils.getFriendlyDateString(context, dateInMillis, false)

        /***********************
         * Weather Description *
         */
        description = SunshineWeatherUtils.getStringForWeatherCondition(context, weatherId)
        /* Create the accessibility (a11y) String from the weather description */
        descriptionA11y = context.getString(R.string.a11y_forecast, description)

        /**************************
         * High (max) temperature *
         */
        /* Read high temperature from the cursor (in degrees celsius) */
        val highInCelsius = cursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP)
        /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        highString = SunshineWeatherUtils.formatTemperature(context, highInCelsius)
        /* Create the accessibility (a11y) String from the weather description */
        highA11y = context.getString(R.string.a11y_high_temp, highString)

        /*************************
         * Low (min) temperature *
         */
        /* Read low temperature from the cursor (in degrees celsius) */
        val lowInCelsius = cursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP)
        /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        lowString = SunshineWeatherUtils.formatTemperature(context, lowInCelsius)
        lowA11y = context.getString(R.string.a11y_low_temp, lowString)
    }
}