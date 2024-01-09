package com.elektro.monitoring.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserLogin(
    val email : String,
    val password : String
): Parcelable
