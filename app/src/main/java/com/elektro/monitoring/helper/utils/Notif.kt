package com.elektro.monitoring.helper.utils

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.elektro.monitoring.R
import com.elektro.monitoring.databinding.CustomToastLayoutBinding
import com.google.android.material.snackbar.Snackbar

fun Context.showToastWithoutIcon(message: String, duration: Int = Toast.LENGTH_SHORT) {
    val inflater = LayoutInflater.from(this)
    val binding: CustomToastLayoutBinding =
        CustomToastLayoutBinding.inflate(inflater)

    binding.toastText.text = message

    val toast = Toast(applicationContext)
    toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 160)
    toast.duration = duration
    toast.view = binding.root
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