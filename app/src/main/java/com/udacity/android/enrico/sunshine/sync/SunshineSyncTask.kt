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
package com.udacity.android.enrico.sunshine.sync

import android.content.Context
import android.text.format.DateUtils
import com.udacity.android.enrico.sunshine.data.SunshinePreferences
import com.udacity.android.enrico.sunshine.data.WeatherContract
import com.udacity.android.enrico.sunshine.gsonResponse.OpenWeatherMapResponse
import com.udacity.android.enrico.sunshine.utilities.Log
import com.udacity.android.enrico.sunshine.utilities.NetworkUtils
import com.udacity.android.enrico.sunshine.utilities.NotificationUtils
import com.udacity.android.enrico.sunshine.utilities.ResponseCallback
import org.jetbrains.anko.error
import java.lang.Exception

object SunshineSyncTask {

    /**
     * Updated 01/18/2018
     *      Switch network request to Retrofit and Gson conversion
     *
     * Performs the network request for updated weather, parses the JSON from that request, and
     * inserts the new weather information into our ContentProvider. Will notify the user that new
     * weather has been loaded if the user hasn't been notified of the weather within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    @Synchronized
    fun syncWeather(context: Context) {

        try {
            val weatherRequestWQueryMap = NetworkUtils.getQueryMap(context)

            NetworkUtils.getResponseFromRetrofit(weatherRequestWQueryMap, object : ResponseCallback {
                override fun onSuccess(response: OpenWeatherMapResponse) {
                    SunshinePreferences.setLocationDetails(
                            context,
                            response.city.coordinates.latitude,
                            response.city.coordinates.longitude
                    )

                    val weatherValues = response.getForecastContentValues()

                    val sunshineContentResolver = context.contentResolver

                    /* Delete old weather data because we don't need to keep multiple days' data */
                    sunshineContentResolver.delete(
                            WeatherContract.WeatherEntry.CONTENT_URI, null, null)

                    /* Insert our new weather data into Sunshine's ContentProvider */
                    sunshineContentResolver.bulkInsert(
                            WeatherContract.WeatherEntry.CONTENT_URI,
                            weatherValues)

                    /*
                         * Finally, after we insert data into the ContentProvider, determine whether or not
                         * we should notify the user that the weather has been refreshed.
                         */
                    val notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context)

                    /*
                     * If the last notification was shown was more than 1 day ago, we want to send
                     * another notification to the user that the weather has been updated. Remember,
                     * it's important that you shouldn't spam your users with notifications.
                     */
                    val timeSinceLastNotification = SunshinePreferences
                            .getEllapsedTimeSinceLastNotification(context)

                    var oneDayPassedSinceLastNotification = false

                    if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                        oneDayPassedSinceLastNotification = true
                    }

                    /*
                     * We only want to show the notification if the user wants them shown and we
                     * haven't shown a notification in the past day.
                     */
                    if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                        NotificationUtils.notifyUserOfNewWeather(context)
                    }

                    /* If the code reaches this point, we have successfully performed our sync */
                }

                override fun onFailure(error: String) {
                    Log.error(error)
                }

            })

//            /*
//             * The getUrl method will return the URL that we need to get the forecast JSON for the
//             * weather. It will decide whether to create a URL based off of the latitude and
//             * longitude or off of a simple location as a String.
//             */
//            val weatherRequestUrl: URL? = NetworkUtils.getUrl(context)
//
//            weatherRequestUrl?.let {
//                /* Use the URL to retrieve the JSON */
//                val jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl)
//
//                jsonWeatherResponse?.let {
//                    /* Parse the JSON into a list of weather values */
//                    val weatherValues = OpenWeatherJsonUtils
//                            .getWeatherContentValuesFromJson(context, jsonWeatherResponse)
//
//                    /*
//                     * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
//                     * would have returned null. We need to check for those cases here to prevent any
//                     * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
//                     * there isn't any to insert.
//                     */
//                    if (weatherValues !=
//                            null && weatherValues.isNotEmpty()) {
//                        /* Get a handle on the ContentResolver to delete and insert data */
//                        val sunshineContentResolver = context.contentResolver
//
//                        /* Delete old weather data because we don't need to keep multiple days' data */
//                        sunshineContentResolver.delete(
//                                WeatherContract.WeatherEntry.CONTENT_URI, null, null)
//
//                        /* Insert our new weather data into Sunshine's ContentProvider */
//                        sunshineContentResolver.bulkInsert(
//                                WeatherContract.WeatherEntry.CONTENT_URI,
//                                weatherValues)
//
//                        /*
//                         * Finally, after we insert data into the ContentProvider, determine whether or not
//                         * we should notify the user that the weather has been refreshed.
//                         */
//                        val notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context)
//
//                        /*
//                         * If the last notification was shown was more than 1 day ago, we want to send
//                         * another notification to the user that the weather has been updated. Remember,
//                         * it's important that you shouldn't spam your users with notifications.
//                         */
//                        val timeSinceLastNotification = SunshinePreferences
//                                .getEllapsedTimeSinceLastNotification(context)
//
//                        var oneDayPassedSinceLastNotification = false
//
//                        if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
//                            oneDayPassedSinceLastNotification = true
//                        }
//
//                        /*
//                         * We only want to show the notification if the user wants them shown and we
//                         * haven't shown a notification in the past day.
//                         */
//                        if (notificationsEnabled && oneDayPassedSinceLastNotification) {
//                            NotificationUtils.notifyUserOfNewWeather(context)
//                        }
//
//                        /* If the code reaches this point, we have successfully performed our sync */
//
//                    }
//                }
//            }

        } catch (e: Exception) {
            /* Server probably invalid */
            e.printStackTrace()
        }

    }
}