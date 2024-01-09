package com.elektro.monitoring.data.network

import android.os.Parcelable

data class CheckState(
    var data: Any? = null,
    val error: String = "",
    val isLoading: Boolean = false
)