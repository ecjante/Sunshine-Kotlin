package com.udacity.android.enrico.sunshine.gsonResponse

/**
 * Created by enrico on 1/18/18.
 */
object OpenWeatherJsonKeys {
    /* Location information */
    const val OWM_CITY = "city"
    const val OWM_COORD = "coord"

    /* Location coordinate */
    const val OWM_LATITUDE = "lat"
    const val OWM_LONGITUDE = "lon"

    /* Weather information. Each day's forecast info is an element of the "list" array */
    const val OWM_LIST = "list"

    const val OWM_PRESSURE = "pressure"
    const val OWM_HUMIDITY = "humidity"
    const val OWM_WINDSPEED = "speed"
    const val OWM_WIND_DIRECTION = "deg"

    /* All temperatures are children of the "temp" object */
    const val OWM_TEMPERATURE = "temp"
    const val OWM_TEMPERATURE_MAIN = "main"

    /* Max temperature for the day */
    const val OWM_MAX = "temp_max"
    const val OWM_MIN = "temp_min"

    const val OWM_WEATHER = "weather"
    const val OWM_WEATHER_ID = "id"

    const val OWM_COUNT = "cnt"
    const val OWM_MESSAGE_CODE = "cod"
}