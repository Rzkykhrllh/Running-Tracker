package com.example.runningtracking.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.example.runningtracking.R
import com.example.runningtracking.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracking.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningtracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracking.other.Constants.ACTION_STOP_SERVICE
import com.example.runningtracking.other.Constants.CHANNEL_ID
import com.example.runningtracking.other.Constants.CHANNEL_NAME
import com.example.runningtracking.other.Constants.NOTIFICATION_ID
import com.example.runningtracking.ui.MainActivity
import com.example.runningtracking.ui.MainActivity_GeneratedInjector
import timber.log.Timber

/* Karena bakal ngeobserve live-data
* dan live data hanya bisa di observe di dengan fungsi observe
* dan fungsi observe membutuhkan lige cycle owner*/

class TrackingService : LifecycleService() {

    private var isFirstRun = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or resumed the service")

                    if (isFirstRun){
                        startNotificationService()
                        isFirstRun = false
                    } else{
                        Timber.d("resumed the service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Pause Service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop Service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startNotificationService(){

//        val notificationManager = NotificationManagerCompat.from(this)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotficationChannel(notificationManager)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .apply {
                setAutoCancel(false) // gak bisa dihilangin
                setOngoing(true) // gabisa di clear make tombol x
                setContentText("Runing App")
                setSubText("00:00:00")
                setContentIntent(getMainActivityPendingIntent())
                setSmallIcon(R.drawable.ic_graph)
                priority = NotificationCompat.PRIORITY_HIGH

            }.build()

        // Start foreground service, yg ada notifnya
        startForeground(NOTIFICATION_ID, notification)

    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )


    @RequiresApi(Build.VERSION_CODES.O) // Sama kek IF, tapi cool way
    private fun createNotficationChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
                .apply {
                    lightColor = Color.GREEN
                    enableLights(true)
                }
        notificationManager.createNotificationChannel(channel)

    }
}