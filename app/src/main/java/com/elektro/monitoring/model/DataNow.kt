package com.elektro.monitoring.model

data class DataNow(
    val soc: Float,
    val arusKeluar: Float,
    val arusMasuk: Float,
    val currentTime: String,
    val suhu: Float,
    val tegangan: Float
){
    constructor() : this(100f, 0f, 0f, "", 0f, 0f)
}