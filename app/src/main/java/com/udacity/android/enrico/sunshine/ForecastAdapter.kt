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
package com.udacity.android.enrico.sunshine

import android.content.Context
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.udacity.android.enrico.sunshine.data.WeatherUiListItemData
import com.udacity.android.enrico.sunshine.databinding.ForecastListItemBinding
import com.udacity.android.enrico.sunshine.databinding.ListItemForecastTodayBinding
import com.udacity.android.enrico.sunshine.utilities.SunshineWeatherUtils

/**
 * [ForecastAdapter] exposes a list of weather forecasts
 * from a [android.database.Cursor] to a [android.support.v7.widget.RecyclerView].
 */
/**
 * Creates a ForecastAdapter.
 *
 * @param context      Used to talk to the UI and app resources
 * @param clickHandler The on-click handler for this adapter. This single handler is called
 * when an item is clicked.
 */
internal class ForecastAdapter (
    /* The context we use to utility methods, app resources and layout inflaters */
        private val mContext: Context,
    /*
     * Below, we've defined an interface to handle clicks on items within this Adapter. In the
     * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
     * said interface. We store that instance in this variable to call the onClick method whenever
     * an item is clicked in the list.
     */
        private val mClickHandler: ForecastAdapterOnClickHandler) : RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder>() {

    /*
     * Flag to determine if we want to use a separate view for the list item that represents
     * today. This flag will be true when the phone is in portrait mode and false when the phone
     * is in landscape. This flag will be set in the constructor of the adapter by accessing
     * boolean resources.
     */
    private val mUseTodayLayout: Boolean = mContext.resources.getBoolean(R.bool.use_today_layout)

    private var mCursor: Cursor? = null

    /**
     * The interface that receives onClick messages.
     */
    interface ForecastAdapterOnClickHandler {
        fun onClick(date: Long)
    }

    /**
     * Updated: 01/18/2018
     *      Changed inflate to DataBindingUtil.inflate to support data binding for each item
     *
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (like ours does) you
     * can use this viewType integer to provide a different layout. See
     * [android.support.v7.widget.RecyclerView.Adapter.getItemViewType]
     * for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ForecastAdapterViewHolder {

        val layoutId: Int
        val binding: ViewDataBinding
        when (viewType) {

            VIEW_TYPE_TODAY -> {
                layoutId = R.layout.list_item_forecast_today
            }

            VIEW_TYPE_FUTURE_DAY -> {
                layoutId = R.layout.forecast_list_item
            }

            else -> throw IllegalArgumentException("Invalid view type, value of " + viewType)
        }

        val inflater = LayoutInflater.from(mContext)
        binding = DataBindingUtil.inflate(inflater, layoutId, viewGroup, false)

        binding.root.isFocusable = true

        return ForecastAdapterViewHolder(binding)
    }

    /**
     * Updated: 01/18/2018
     *      Changed functionality to data binding functionality, setting the layouts bound
     *      weatherData
     *
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent the
     * contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(forecastAdapterViewHolder: ForecastAdapterViewHolder, position: Int) {
        mCursor!!.moveToPosition(position)

        /****************
         * Weather Icon *
         */
        val weatherId = mCursor!!.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID)
        val weatherImageId: Int

        val viewType = getItemViewType(position)

        when (viewType) {

            VIEW_TYPE_TODAY -> weatherImageId = SunshineWeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId)

            VIEW_TYPE_FUTURE_DAY -> weatherImageId = SunshineWeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId)

            else -> throw IllegalArgumentException("Invalid view type, value of " + viewType)
        }

        val weatherListItemData = WeatherUiListItemData(mContext, mCursor!!, weatherImageId)

        if (forecastAdapterViewHolder.binding is ListItemForecastTodayBinding) {
            forecastAdapterViewHolder.binding.weatherData = weatherListItemData
        } else if (forecastAdapterViewHolder.binding is ForecastListItemBinding) {
            forecastAdapterViewHolder.binding.weatherData = weatherListItemData
        }
        forecastAdapterViewHolder.binding.executePendingBindings()
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    override fun getItemCount(): Int {
        return mCursor?.count ?: 0
    }

    /**
     * Returns an integer code related to the type of View we want the ViewHolder to be at a given
     * position. This method is useful when we want to use different layouts for different items
     * depending on their position. In Sunshine, we take advantage of this method to provide a
     * different layout for the "today" layout. The "today" layout is only shown in portrait mode
     * with the first item in the list.
     *
     * @param position index within our RecyclerView and Cursor
     * @return the view type (today or future day)
     */
    override fun getItemViewType(position: Int): Int {
        return if (mUseTodayLayout && position == 0) {
            VIEW_TYPE_TODAY
        } else {
            VIEW_TYPE_FUTURE_DAY
        }
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    fun swapCursor(newCursor: Cursor?) {
        mCursor = newCursor
        notifyDataSetChanged()
    }

    /**
     * Updated: 01/18/2018
     *      View holder now holds reference to the ViewDataBinding
     *
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    internal inner class ForecastAdapterViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param v the View that was clicked
         */
        override fun onClick(v: View) {
            val adapterPosition = adapterPosition
            mCursor!!.moveToPosition(adapterPosition)
            val dateInMillis = mCursor!!.getLong(MainActivity.INDEX_WEATHER_DATE)
            mClickHandler.onClick(dateInMillis)
        }
    }

    companion object {
        private val VIEW_TYPE_TODAY = 0
        private val VIEW_TYPE_FUTURE_DAY = 1
    }
}