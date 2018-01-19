package com.udacity.android.enrico.sunshine.gsonResponse

import android.content.ContentValues
import com.google.gson.annotations.SerializedName
import com.udacity.android.enrico.sunshine.utilities.SunshineDateUtils
import org.jetbrains.anko.collections.forEachWithIndex

/**
 * Created by enrico on 1/18/18.
 */
data class OpenWeatherMapResponse(
        val city: City,
        @SerializedName(OpenWeatherJsonKeys.OWM_MESSAGE_CODE)
        val messageCode: Int,
        @SerializedName(OpenWeatherJsonKeys.OWM_COUNT)
        val count: Int,
        @SerializedName(OpenWeatherJsonKeys.OWM_LIST)
        val forecasts: List<Forecast>
) {
    fun fixDates() {
        val normalizedUtcStartDay = SunshineDateUtils.getNormalizedUtcDateForToday()
        forecasts.forEachWithIndex { i, forecast ->
            val dateTimeMillis = normalizedUtcStartDay + SunshineDateUtils.DAY_IN_MILLIS * i
            forecast.date = dateTimeMillis
        }
    }

    fun getForecastContentValues(): Array<ContentValues> {
        val contentValues = forecasts.mapTo(ArrayList()) {
            it.generateContentValues()
        }
        return contentValues.toTypedArray()
    }
}