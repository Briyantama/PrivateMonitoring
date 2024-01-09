package com.elektro.monitoring.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var email: String,
    var image: String,
    var job: String? = "karyawan",
    var name: String,
    var phoneNumber: String,
): Parcelable {
    // Constructor dengan parameter nol-arg
    constructor() : this("", "", "", "", "")

    // Konstruktor tambahan jika Anda perlu
    constructor(email: String, image: String, name: String, phoneNumber: String) : this(email, image, "karyawan", name, phoneNumber)
}