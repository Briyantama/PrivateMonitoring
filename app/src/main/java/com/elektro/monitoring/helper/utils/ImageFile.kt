package com.elektro.monitoring.helper.utils

import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elektro.monitoring.R
import com.elektro.monitoring.viewmodel.AuthViewModel

fun loadImage(uri: Any, view: View, image: ImageView) {
    uri.let {

        Glide.with(view)
            .load(it)
            .centerCrop()
            .apply(RequestOptions().placeholder(R.drawable.ic_select_photo))
            .into(image)
    }
}