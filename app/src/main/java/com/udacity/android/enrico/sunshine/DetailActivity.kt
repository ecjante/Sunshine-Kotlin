/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.udacity.android.enrico.sunshine

import android.content.Intent
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.app.ShareCompat
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.udacity.android.enrico.sunshine.data.WeatherContract
import com.udacity.android.enrico.sunshine.data.WeatherUiData
import com.udacity.android.enrico.sunshine.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private var mForecastSummary: String? = null

    /* The URI that is used to access the chosen day's weather details */
    private var mUri: Uri? = null


    /*
     * This field is used for data binding. Normally, we would have to call findViewById many
     * times to get references to the Views in this Activity. With data binding however, we only
     * need to call DataBindingUtil.setContentView and pass in a Context and a layout, as we do
     * in onCreate of this class. Then, we can access all of the Views in our layout
     * programmatically without cluttering up the code with findViewById.
     */
    private lateinit var mDetailBinding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail)

        mUri = intent.data
        if (mUri == null) throw NullPointerException("URI for DetailActivity cannot be null")

        /* This connects our Activity into the loader lifecycle. */
        supportLoaderManager.initLoader(ID_DETAIL_LOADER, null, this)
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     *
     * @see android.app.Activity.onPrepareOptionsMenu
     * @see .onOptionsItemSelected
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        val inflater = menuInflater
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu)
        /* Return true so that the menu is displayed in the Toolbar */
        return true
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu. Android will
     * automatically handle clicks on the "up" button for us so long as we have specified
     * DetailActivity's parent Activity in the AndroidManifest.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /* Get the ID of the clicked item */
        val id = item.itemId

        /* Settings menu item clicked */
        if (id == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }

        /* Share menu item clicked */
        if (id == R.id.action_share) {
            val shareIntent = createShareForecastIntent()
            startActivity(shareIntent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Uses the ShareCompat Intent builder to create our Forecast intent for sharing.  All we need
     * to do is set the type, text and the NEW_DOCUMENT flag so it treats our share as a new task.
     * See: http://developer.android.com/guide/components/tasks-and-back-stack.html for more info.
     *
     * @return the Intent to use to share our weather forecast
     */
    private fun createShareForecastIntent(): Intent {
        val shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText("$mForecastSummary$FORECAST_SHARE_HASHTAG")
                .intent
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        return shareIntent
    }

    /**
     * Creates and returns a CursorLoader that loads the data for our URI and stores it in a Cursor.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param loaderArgs Any arguments supplied by the caller
     *
     * @return A new Loader instance that is ready to start loading.
     */
    override fun onCreateLoader(loaderId: Int, loaderArgs: Bundle?): Loader<Cursor> {

        when (loaderId) {

            ID_DETAIL_LOADER ->

                return CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION, null, null, null)

            else -> throw RuntimeException("Loader Not Implemented: $loaderId")
        }
    }

    /**
     * Updated 01/18/2018
     *      Change binding to use layout data variable binding instead of setting the data in code
     *
     * Runs on the main thread when a load is complete. If initLoader is called (we call it from
     * onCreate in DetailActivity) and the LoaderManager already has completed a previous load
     * for this Loader, onLoadFinished will be called immediately. Within onLoadFinished, we bind
     * the data to our views so the user can see the details of the weather on the date they
     * selected from the forecast.
     *
     * @param loader The cursor loader that finished.
     * @param data   The cursor that is being returned.
     */
    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

        /*
         * Before we bind the data to the UI that will display that data, we need to check the
         * cursor to make sure we have the results that we are expecting. In order to do that, we
         * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
         * Although it may not seem obvious at first, moveToFirst will return true if it contains
         * a valid first row of data.
         *
         * If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.
         */
        var cursorHasValidData = false
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return
        }

        data?.let {
            val weatherData = WeatherUiData(this, data)

            mDetailBinding.weather = weatherData
            mDetailBinding.executePendingBindings()

            /* Store the forecast summary String in our forecast summary field to share later */
            mForecastSummary = String.format("%s - %s - %s/%s",
                    weatherData.dateText, weatherData.description, weatherData.highString, weatherData.lowString)
        }

//        /****************
//         * Weather Icon *
//         */
//        /* Read weather condition ID from the cursor (ID provided by Open Weather Map) */
//        val weatherId = data!!.getInt(INDEX_WEATHER_CONDITION_ID)
//        /* Use our utility method to determine the resource ID for the proper art */
//        val weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId)
//
//        /* Set the resource ID on the icon to display the art */
//        mDetailBinding!!.primaryInfo.weatherIcon.setImageResource(weatherImageId)
//
//        /****************
//         * Weather Date *
//         */
//        /*
//         * Read the date from the cursor. It is important to note that the date from the cursor
//         * is the same date from the weather SQL table. The date that is stored is a GMT
//         * representation at midnight of the date when the weather information was loaded for.
//         *
//         * When displaying this date, one must add the GMT offset (in milliseconds) to acquire
//         * the date representation for the local date in local time.
//         * SunshineDateUtils#getFriendlyDateString takes care of this for us.
//         */
//        val localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE)
//        val dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true)
//
//        mDetailBinding!!.primaryInfo.date.setText(dateText)
//
//        /***********************
//         * Weather Description *
//         */
//        /* Use the weatherId to obtain the proper description */
//        val description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId)
//
//        /* Create the accessibility (a11y) String from the weather description */
//        val descriptionA11y = getString(R.string.a11y_forecast, description)
//
//        /* Set the text and content description (for accessibility purposes) */
//        mDetailBinding!!.primaryInfo.weatherDescription.setText(description)
//        mDetailBinding!!.primaryInfo.weatherDescription.setContentDescription(descriptionA11y)
//
//        /* Set the content description on the weather image (for accessibility purposes) */
//        mDetailBinding!!.primaryInfo.weatherIcon.setContentDescription(descriptionA11y)
//
//        /**************************
//         * High (max) temperature *
//         */
//        /* Read high temperature from the cursor (in degrees celsius) */
//        val highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP)
//        /*
//         * If the user's preference for weather is fahrenheit, formatTemperature will convert
//         * the temperature. This method will also append either °C or °F to the temperature
//         * String.
//         */
//        val highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius)
//
//        /* Create the accessibility (a11y) String from the weather description */
//        val highA11y = getString(R.string.a11y_high_temp, highString)
//
//        /* Set the text and content description (for accessibility purposes) */
//        mDetailBinding!!.primaryInfo.highTemperature.setText(highString)
//        mDetailBinding!!.primaryInfo.highTemperature.setContentDescription(highA11y)
//
//        /*************************
//         * Low (min) temperature *
//         */
//        /* Read low temperature from the cursor (in degrees celsius) */
//        val lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP)
//        /*
//         * If the user's preference for weather is fahrenheit, formatTemperature will convert
//         * the temperature. This method will also append either °C or °F to the temperature
//         * String.
//         */
//        val lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius)
//
//        val lowA11y = getString(R.string.a11y_low_temp, lowString)
//
//        /* Set the text and content description (for accessibility purposes) */
//        mDetailBinding!!.primaryInfo.lowTemperature.setText(lowString)
//        mDetailBinding!!.primaryInfo.lowTemperature.setContentDescription(lowA11y)
//
//        /************
//         * Humidity *
//         */
//        /* Read humidity from the cursor */
//        val humidity = data.getFloat(INDEX_WEATHER_HUMIDITY)
//        val humidityString = getString(R.string.format_humidity, humidity)
//
//        val humidityA11y = getString(R.string.a11y_humidity, humidityString)
//
//        /* Set the text and content description (for accessibility purposes) */
//        mDetailBinding!!.extraDetails.humidity.setText(humidityString)
//        mDetailBinding!!.extraDetails.humidity.setContentDescription(humidityA11y)
//
//        mDetailBinding!!.extraDetails.humidityLabel.setContentDescription(humidityA11y)
//
//        /****************************
//         * Wind speed and direction *
//         */
//        /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
//        val windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED)
//        val windDirection = data.getFloat(INDEX_WEATHER_DEGREES)
//        val windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection)
//
//        val windA11y = getString(R.string.a11y_wind, windString)
//
//        /* Set the text and content description (for accessibility purposes) */
//        mDetailBinding!!.extraDetails.windMeasurement.setText(windString)
//        mDetailBinding!!.extraDetails.windMeasurement.setContentDescription(windA11y)
//
//        mDetailBinding!!.extraDetails.windLabel.setContentDescription(windA11y)
//
//        /************
//         * Pressure *
//         */
//        /* Read pressure from the cursor */
//        val pressure = data.getFloat(INDEX_WEATHER_PRESSURE)
//
//        /*
//         * Format the pressure text using string resources. The reason we directly access
//         * resources using getString rather than using a method from SunshineWeatherUtils as
//         * we have for other data displayed in this Activity is because there is no
//         * additional logic that needs to be considered in order to properly display the
//         * pressure.
//         */
//        val pressureString = getString(R.string.format_pressure, pressure)
//
//        val pressureA11y = getString(R.string.a11y_pressure, pressureString)
//
//        /* Set the text and content description (for accessibility purposes) */
//        mDetailBinding!!.extraDetails.pressure.setText(pressureString)
//        mDetailBinding!!.extraDetails.pressure.setContentDescription(pressureA11y)
//
//        mDetailBinding!!.extraDetails.pressureLabel.setContentDescription(pressureA11y)
    }

    /**
     * Called when a previously created loader is being reset, thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     * Since we don't store any of this cursor's data, there are no references we need to remove.
     *
     * @param loader The Loader that is being reset.
     */
    override fun onLoaderReset(loader: Loader<Cursor>) {}

    companion object {

        /*
         * In this Activity, you can share the selected day's forecast. No social sharing is complete
         * without using a hashtag. #BeTogetherNotTheSame
         */
        private const val FORECAST_SHARE_HASHTAG = " #SunshineApp"

        /*
         * The columns of data that we are interested in displaying within our DetailActivity's
         * weather display.
         */
        val WEATHER_DETAIL_PROJECTION = arrayOf(
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_DEGREES,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
        )

        /*
         * We store the indices of the values in the array of Strings above to more quickly be able
         * to access the data from our query. If the order of the Strings above changes, these
         * indices must be adjusted to match the order of the Strings.
         */
        const val INDEX_WEATHER_DATE = 0
        const val INDEX_WEATHER_MAX_TEMP = 1
        const val INDEX_WEATHER_MIN_TEMP = 2
        const val INDEX_WEATHER_HUMIDITY = 3
        const val INDEX_WEATHER_PRESSURE = 4
        const val INDEX_WEATHER_WIND_SPEED = 5
        const val INDEX_WEATHER_DEGREES = 6
        const val INDEX_WEATHER_CONDITION_ID = 7

        /*
         * This ID will be used to identify the Loader responsible for loading the weather details
         * for a particular day. In some cases, one Activity can deal with many Loaders. However, in
         * our case, there is only one. We will still use this ID to initialize the loader and create
         * the loader for best practice. Please note that 353 was chosen arbitrarily. You can use
         * whatever number you like, so long as it is unique and consistent.
         */
        private const val ID_DETAIL_LOADER = 353
    }
}