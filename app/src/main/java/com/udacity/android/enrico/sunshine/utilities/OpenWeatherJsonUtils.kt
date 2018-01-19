/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.udacity.android.enrico.sunshine.utilities

import android.content.ContentValues
import android.content.Context
import com.udacity.android.enrico.sunshine.data.SunshinePreferences
import com.udacity.android.enrico.sunshine.data.WeatherContract

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.net.HttpURLConnection

/**
 * Utility functions to handle OpenWeatherMap JSON data.
 */
object OpenWeatherJsonUtils {

    /* Location information */
    private val OWM_CITY = "city"
    private val OWM_COORD = "coord"

    /* Location coordinate */
    private val OWM_LATITUDE = "lat"
    private val OWM_LONGITUDE = "lon"

    /* Weather information. Each day's forecast info is an element of the "list" array */
    private val OWM_LIST = "list"

    private val OWM_PRESSURE = "pressure"
    private val OWM_HUMIDITY = "humidity"
    private val OWM_WINDSPEED = "speed"
    private val OWM_WIND_DIRECTION = "deg"

    /* All temperatures are children of the "temp" object */
    private val OWM_TEMPERATURE = "temp"

    /* Max temperature for the day */
    private val OWM_MAX = "max"
    private val OWM_MIN = "min"

    private val OWM_WEATHER = "weather"
    private val OWM_WEATHER_ID = "id"

    private val OWM_MESSAGE_CODE = "cod"

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     *
     *
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullWeatherDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param forecastJsonStr JSON response from server
     *
     * @return Array of Strings describing weather data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    @Throws(JSONException::class)
    fun getWeatherContentValuesFromJson(context: Context, forecastJsonStr: String): Array<ContentValues>? {

        val forecastJson = JSONObject(forecastJsonStr)

        /* Is there an error? */
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            val errorCode = forecastJson.getInt(OWM_MESSAGE_CODE)

            when (errorCode) {
                HttpURLConnection.HTTP_OK -> {
                }
                HttpURLConnection.HTTP_NOT_FOUND ->
                    /* Location invalid */
                    return null
                else ->
                    /* Server probably down */
                    return null
            }
        }

        val jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST)

        val cityJson = forecastJson.getJSONObject(OWM_CITY)

        val cityCoord = cityJson.getJSONObject(OWM_COORD)
        val cityLatitude = cityCoord.getDouble(OWM_LATITUDE)
        val cityLongitude = cityCoord.getDouble(OWM_LONGITUDE)

        SunshinePreferences.setLocationDetails(context, cityLatitude, cityLongitude)

        val weatherContentValues = Array<ContentValues>(jsonWeatherArray.length()) { ContentValues() }

        /*
         * OWM returns daily forecasts based upon the local time of the city that is being asked
         * for, which means that we need to know the GMT offset to translate this data properly.
         * Since this data is also sent in-order and the first day is always the current day, we're
         * going to take advantage of that to get a nice normalized UTC date for all of our weather.
         */
        //        long now = System.currentTimeMillis();
        //        long normalizedUtcStartDay = SunshineDateUtils.normalizeDate(now);

        val normalizedUtcStartDay = SunshineDateUtils.getNormalizedUtcDateForToday()

        for (i in 0 until jsonWeatherArray.length()) {

            val dateTimeMillis: Long
            val pressure: Double
            val humidity: Int
            val windSpeed: Double
            val windDirection: Double

            val high: Double
            val low: Double

            val weatherId: Int

            /* Get the JSON object representing the day */
            val dayForecast = jsonWeatherArray.getJSONObject(i)

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */
            dateTimeMillis = normalizedUtcStartDay + SunshineDateUtils.DAY_IN_MILLIS * i

            pressure = dayForecast.getDouble(OWM_PRESSURE)
            humidity = dayForecast.getInt(OWM_HUMIDITY)
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED)
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION)

            /*
             * Description is in a child array called "weather", which is 1 element long.
             * That element also contains a weather code.
             */
            val weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0)

            weatherId = weatherObject.getInt(OWM_WEATHER_ID)

            /*
             * Temperatures are sent by Open Weather Map in a child object called "temp".
             *
             * Editor's Note: Try not to name variables "temp" when working with temperature.
             * It confuses everybody. Temp could easily mean any number of things, including
             * temperature, temporary variable, temporary folder, temporary employee, or many
             * others, and is just a bad variable name.
             */
            val temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE)
            high = temperatureObject.getDouble(OWM_MAX)
            low = temperatureObject.getDouble(OWM_MIN)

            val weatherValues = ContentValues()
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTimeMillis)
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity)
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure)
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed)
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection)
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high)
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low)
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId)

            weatherContentValues[i] = weatherValues
        }

        return weatherContentValues
    }
}