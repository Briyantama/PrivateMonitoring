package com.elektro.monitoring.model

data class Data10Min(
    val soc: Float,
    val arusKeluar: Float,
    val arusMasuk: Float,
    val currentTime: String,
    val tegangan: Float
) {
    constructor() : this(100f, 0f, 0f, "", 0f)
}