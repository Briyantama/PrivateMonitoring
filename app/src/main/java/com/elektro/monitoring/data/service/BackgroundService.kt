package com.elektro.monitoring.data.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.elektro.monitoring.data.repo.NotificationRepository
import com.elektro.monitoring.helper.Constants.TAG
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.helper.utils.convertMiliSecond
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

    private val fireDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private val msdf = SimpleDateFormat("HH:mm:ss,SSS", Locale.getDefault())
    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val tf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val tfLengkap = SimpleDateFormat("EEEE, dd-MMMM-yyyy", Locale.getDefault())

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
        stopSelf()
    }

    private val runnable = object : Runnable {
        override fun run() {
            val sharedPrefData = SharedPrefData(application)
            val mCurrentTime = msdf.format(Calendar.getInstance().time)
            val currentTime = convertMiliSecond(mCurrentTime)
            val tanggal = tf.format(Calendar.getInstance().time)
            val date = sharedPrefData.callDataString("dateToday")
            sharedPrefData.editDataString("today", tfLengkap.format(Calendar.getInstance().time))

            Log.d(TAG, "run: berjalan")

            fireDatabase.getReference("notif").limitToLast(1)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { childSnapshot ->
                            childSnapshot.key?.toInt()?.let {
                                sharedPrefData.editDataInt("sizeNotif", it) }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

            fireDatabase.getReference("panels/Solar A/$date").limitToLast(1)
                .addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { childSnapshot ->
                            childSnapshot.key?.let {
                                sharedPrefData.editDataInt("sizeData", it.toInt()) }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })

            fireDatabase.getReference("dataNow")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newData = snapshot.getValue(DataNow::class.java)
                        val lastNotifTime = sharedPrefData.callDataInt("lastNotifTime")
                        val sizeNotif = sharedPrefData.callDataInt("sizeNotif") + 1
                        val diff = currentTime-lastNotifTime

                        if (newData != null) {
                            if (diff >= 300000 && newData.suhu >= 35f) {
                                Log.d("Notification Test", "onDataChange: $diff")
                                val jam = sdf.format(Calendar.getInstance().time)

                                notificationRepository.sendNotification(
                                    applicationContext,
                                    "Suhu tinggi: ${newData.suhu}°C",
                                    "Watercooling menyala"
                                )

                                val notifSuhu = NotifikasiSuhu(
                                    tanggal,
                                    jam,
                                    "Suhu tinggi: ${newData.suhu}°C",
                                    "Watercooling menyala"
                                )

                                fireDatabase.getReference("notif").child(sizeNotif.toString()).setValue(notifSuhu)
                                sharedPrefData.editDataInt("lastNotifTime", currentTime)
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