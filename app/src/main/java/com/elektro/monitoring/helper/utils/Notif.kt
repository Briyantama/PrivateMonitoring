package com.elektro.monitoring.helper.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.elektro.monitoring.databinding.CustomToastLayoutBinding

fun Context.showToastWithoutIcon(message: String, duration: Int = Toast.LENGTH_SHORT) {
    val inflater = LayoutInflater.from(this)
    val binding: CustomToastLayoutBinding =
        CustomToastLayoutBinding.inflate(inflater)

    binding.toastText.text = message

    val toast = Toast(applicationContext)
    toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 160)
    toast.duration = duration
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        toast.addCallback(object : Toast.Callback(){
            override fun onToastShown() {
                Log.d("ToastCallback", "Toast shown")
            }

            override fun onToastHidden() {
                Log.d("ToastCallback", "Toast hidden")
            }
        })
    }
    toast.show()
}

fun Context.dialogPN(
        judul: String,
        message: String,
        positif: String,
        negatif: String,
        action: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(judul)
            .setMessage(message)
            .setPositiveButton(positif) { _, _ -> action() }
            .setNegativeButton(negatif) { listener, _ -> listener.dismiss() }
            .show()
    }