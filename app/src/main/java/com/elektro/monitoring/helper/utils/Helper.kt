package com.elektro.monitoring.helper.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun convertBulan(bulan: String): String {
    return when (bulan) {
        "01" -> "January"
        "02" -> "February"
        "03" -> "March"
        "04" -> "April"
        "05" -> "May"
        "06" -> "June"
        "07" -> "July"
        "08" -> "August"
        "09" -> "September"
        "10" -> "October"
        "11" -> "November"
        "12" -> "December"
        else -> "Bulan tidak valid"
    }
}

fun convertMinggu(minggu: String): String {
    return when (minggu) {
        "1" -> "Minggu ke-1"
        "2" -> "Minggu ke-2"
        "3" -> "Minggu ke-3"
        "4" -> "Minggu ke-4"
        "5" -> "Minggu ke-5"
        else -> "MInggu tidak valid"
    }
}

fun invertBulan(bulan: String): String {
    return when (bulan) {
        "January" -> "01"
        "February" -> "02"
        "March" -> "03"
        "April" -> "04"
        "May" -> "05"
        "June" -> "06"
        "July" -> "07"
        "August" -> "08"
        "September" -> "09"
        "October" -> "10"
        "November" -> "11"
        "December" -> "12"
        else -> "Bulan tidak valid"
    }
}

fun invertMinggu(minggu: String): String {
    return when (minggu) {
        "Minggu ke-1" -> "1"
        "Minggu ke-2" -> "2"
        "Minggu ke-3" -> "3"
        "Minggu ke-4" -> "4"
        "Minggu ke-5" -> "5"
        else -> "MInggu tidak valid"
    }
}

fun getWeekNumber(date: String): Int {
    val sdf = SimpleDateFormat("EEEE, dd-MMMM-yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance().apply {
        time = sdf.parse(date) as Date
    }
    calendar.firstDayOfWeek = Calendar.MONDAY
    return calendar.get(Calendar.WEEK_OF_MONTH)
}

fun getMonth(dateString: String): String {
    return dateString.split("-")[1]
}

fun getTanggal(dateString: String): String {
    val parts = dateString.split(", ")[1].split("-")
    return parts[0].trim()
}

fun convertMiliSecond(waktu: String): Int {
    val sdf = SimpleDateFormat("HH:mm:ss,SSS", Locale.getDefault())
    val date = sdf.parse(waktu)

    val cal = Calendar.getInstance()
    if (date != null) {
        cal.time = date
    }

    val jamMilidetik = cal.get(Calendar.HOUR_OF_DAY) * 3600000
    val menitMilidetik = cal.get(Calendar.MINUTE) * 60000
    val detikMilidetik = cal.get(Calendar.SECOND) * 1000
    val milidetik = cal.get(Calendar.MILLISECOND)

    return jamMilidetik + menitMilidetik + detikMilidetik + milidetik
}