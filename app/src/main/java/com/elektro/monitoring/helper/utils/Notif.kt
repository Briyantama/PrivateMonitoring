package com.elektro.monitoring.helper.utils

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.elektro.monitoring.R
import com.google.android.material.snackbar.Snackbar

fun Context.showToast(message : String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun View.showSnackbar(message: String) =
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()

fun View.showSnackbarWithAction(
        message: String,
        actionText: String,
        action: () -> Any) {
        val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction(actionText) { action.invoke() }
        snackBar.show()
    }

fun Context.showSnackbarGreen(
        message: String,
        view: View,
        action: () -> Any
    ) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.green))
            .setActionTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.setAction("X") { action.invoke() }
        snackBar.show()
    }

fun Context.showSnackbarRed(
        message: String,
        view: View,
        action: () -> Any
    ) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.red))
            .setActionTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.setAction("X") { action.invoke() }
        snackBar.show()
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