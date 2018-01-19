package com.udacity.android.enrico.sunshine.data

import android.databinding.BindingAdapter
import android.widget.ImageView

/**
 * Created by enrico on 1/18/18.
 */
object BindingAdapters {

    @JvmStatic
    @BindingAdapter("bind:imageResource")
    fun setImageResource(imageView: ImageView, resourceId: Int) {
        imageView.setImageResource(resourceId)
    }

}