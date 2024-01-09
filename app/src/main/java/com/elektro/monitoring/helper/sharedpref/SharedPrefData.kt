package com.elektro.monitoring.helper.sharedpref

import android.app.Application
import android.content.Context
import javax.inject.Inject

class SharedPrefData @Inject constructor(application: Application) {
    private val sharedPreferencesSoC = application.getSharedPreferences("SoC", Context.MODE_PRIVATE)
    private val sharedPreferencesFloat = application.getSharedPreferences("Float", Context.MODE_PRIVATE)
    private val sharedPreferencesData = application.getSharedPreferences("Data", Context.MODE_PRIVATE)
    private val sharefPreferencesAuth = application.getSharedPreferences("Auth", Context.MODE_PRIVATE)

    fun totalSoC(): Float {
        return sharedPreferencesSoC.getFloat("soc", 100.0f)
    }

    fun increaseSoC(value: Float) {
        val new = value+1f
        sharedPreferencesSoC.edit().putFloat("soc", new).apply()
    }

    fun decreaseSoC(value: Float) {
        val new = value-1f
        sharedPreferencesSoC.edit().putFloat("soc", new).apply()
    }

    fun sumData(key: String, value: Float, newValue: Float) {
        val new = value+newValue
        sharedPreferencesFloat.edit().putFloat(key, new).apply()
    }

    fun totalData(key: String): Float {
        return sharedPreferencesFloat.getFloat(key, 0.0f)
    }

    fun callDataString(key: String): String {
        return sharedPreferencesData.getString(key, "").toString()
    }

    fun editDataString(key: String, value: String) {
        sharedPreferencesData.edit().putString(key, value).apply()
    }

    fun callDataInt(key: String): Int {
        return sharedPreferencesData.getInt(key, 0)
    }

    fun editDataInt(key: String, value: Int) {
        sharedPreferencesData.edit().putInt(key, value).apply()
    }

    fun clearFloat() {
        sharedPreferencesFloat.edit().clear().apply()
    }

    fun saveData(key: String, value: String) {
        sharefPreferencesAuth.edit().putString(key, value).apply()
    }

    fun loadData(key: String): String {
        return sharefPreferencesAuth.getString(key, "") ?: ""
    }

    fun clearAuth() {
        sharefPreferencesAuth.edit().clear().apply()
    }
}