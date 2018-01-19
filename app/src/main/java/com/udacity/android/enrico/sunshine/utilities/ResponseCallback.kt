package com.udacity.android.enrico.sunshine.utilities

import com.udacity.android.enrico.sunshine.gsonResponse.OpenWeatherMapResponse

/**
 * Created by enrico on 1/18/18.
 */
interface ResponseCallback {
    fun onSuccess(response: OpenWeatherMapResponse)
    fun onFailure(error: String)
}