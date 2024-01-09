package com.elektro.monitoring.data.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.elektro.monitoring.data.repo.NotificationRepository
import com.elektro.monitoring.helper.Constants.NOTIFICATION_ID
import com.elektro.monitoring.helper.Constants.TAG
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.model.DataNow
import com.elektro.monitoring.model.NotifikasiSuhu
import com.elektro.monitoring.viewmodel.DataViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BackgroundService: Service() {

    @Inject
    lateinit var dataViewModel: DataViewModel
    @Inject
    lateinit var notificationRepository: NotificationRepository

    private var lastNotificationTime: Long = 0
    private val fireDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private val sdf = SimpleDateFormat("HH:mm:ss,SSS", Locale.getDefault())
    private val tf = SimpleDateFormat("EEEE, dd-MMMM-yyyy", Locale.getDefault())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        handler.post(runnable)

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: hancur")
        handler.removeCallbacks(runnable)
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private val runnable = object : Runnable {
        override fun run() {
            val sharedPrefData = SharedPrefData(application)
            val currentTime = System.currentTimeMillis()
            val selectedPanel = sharedPrefData.callDataString("selectedpanel")
            val tanggal: String = tf.format(Calendar.getInstance().time)
            Log.d(TAG, "run: berjalan")

            fireDatabase.getReference("notif").limitToLast(1)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (childSnapshot in snapshot.children) {
                            childSnapshot.key?.toInt()?.let {
                                sharedPrefData.editDataInt("sizeNotif", it) }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

            fireDatabase.getReference("panels").child(selectedPanel).child(tanggal)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        sharedPrefData.editDataInt("sizeData", snapshot.childrenCount.toInt())
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

            fireDatabase.getReference("dataNow")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newData = snapshot.getValue(DataNow::class.java)
                        val sizeNotif = sharedPrefData.callDataInt("sizeNotif") + 1

                        if (newData != null) {
                            if (currentTime - lastNotificationTime >= 10000 && newData.suhu >= 35f) {
                                val jam = sdf.format(Calendar.getInstance().time)
                                startForeground(NOTIFICATION_ID,
                                    notificationRepository.sendNotification(
                                        applicationContext,
                                        "Suhu tinggi: ${newData.suhu}°C",
                                        "Watercooling menyala"
                                    )
                                )

                                val notifSuhu = NotifikasiSuhu(
                                    tanggal,
                                    jam,
                                    "Suhu tinggi: ${newData.suhu}°C",
                                    "Watercooling menyala"
                                )
                                fireDatabase.getReference("notif").child(sizeNotif.toString()).setValue(notifSuhu)
                                lastNotificationTime = currentTime
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            handler.postDelayed(this, 1000)
        }
    }
}