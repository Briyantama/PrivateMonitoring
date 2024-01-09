package com.elektro.monitoring.model

data class NotifikasiSuhu(
    val hari: String,
    val time: String,
    val title: String,
    val text: String
){
    constructor() : this("","", "", "")
}
