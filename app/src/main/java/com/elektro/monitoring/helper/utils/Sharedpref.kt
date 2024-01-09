package com.elektro.monitoring.helper.utils

import android.content.Context
import android.content.SharedPreferences
import com.elektro.monitoring.helper.Constants.SHARED_PREFERENCES_FILE_NAME

fun Context.sharedPref() {

    val sharedPref = this.getSharedPreferences(
        SHARED_PREFERENCES_FILE_NAME,
        Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPref.edit()

    fun putBooleanKey(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun getBooleanKey(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }

    fun putStringKey(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    fun getStringKey(key: String): String? {
        return sharedPref.getString(key, null)
    }

    fun clear(){
        editor.clear().apply()
    }
}
