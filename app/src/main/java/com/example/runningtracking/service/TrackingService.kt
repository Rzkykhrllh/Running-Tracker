package com.example.runningtracking.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningtracking.R
import com.example.runningtracking.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracking.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningtracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracking.other.Constants.ACTION_STOP_SERVICE
import com.example.runningtracking.other.Constants.CHANNEL_ID
import com.example.runningtracking.other.Constants.CHANNEL_NAME
import com.example.runningtracking.other.Constants.FASTEST_LOCATION_UPDATE
import com.example.runningtracking.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningtracking.other.Constants.NOTIFICATION_ID
import com.example.runningtracking.other.Constants.TIMER_DELAY
import com.example.runningtracking.other.TrackingUtility
import com.example.runningtracking.ui.MainActivity
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/* Jadi garis adalah kumpulan titik-titik
* dan kita akan memiliki kumpulan garis*/
typealias polyline = MutableList<LatLng>
typealias polylines = MutableList<polyline>


/* Karena bakal ngeobserve live-data
* dan live data hanya bisa di observe di dengan fungsi observe
* dan fungsi observe membutuhkan life cycle owner*/
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient // Objek Buat request lokasi

    private var timeRunInSeconds = MutableLiveData<Long>() // Waktu buat update notfikasi

    @Inject
    lateinit var baseNotificationBuilder : NotificationCompat.Builder

    lateinit var curNotificationBuilder : NotificationCompat.Builder


    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<polylines>() // Live data of list of line

        var timeRunInMillis = MutableLiveData<Long>() // Waktu untuk ditampilkan di Timer TrackingFragment
    }


    // Init value awal dari livedata
    private fun postInitValue() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    // create service class
    override fun onCreate() {
        super.onCreate()
        Timber.d("Service-desu onCreate")

        postInitValue()

        curNotificationBuilder = baseNotificationBuilder

        // Observe isTracking
        isTracking.observe(this, Observer {
            updateLocationTracking(it) // start request location from maps
            updateNotificationTrackingState(it) // change action in notification
        })
    }

    // Start Service from fragment
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Service-desu onStartCommand")

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> { // Start-resume service
                    Timber.d("Started or resumed the service")

                    if (isFirstRun) {
                        startForegroundService() // set notification, start timer
                        isFirstRun = false
                    } else {
                        Timber.d("resumed the service")
                        startTimer()

                    }
                }
                ACTION_PAUSE_SERVICE -> { // Pause Service
                    Timber.d("Pause Service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> { // Stop Service
                    Timber.d("Stop Service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // Start Tracking service
    private fun startForegroundService() {

        startTimer()
        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotficationChannel(notificationManager)
        }


        // Start foreground service, yg ada notifnya
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        // Update timer di notifikasi
        timeRunInSeconds.observe(this, Observer {

            if (!serviceKilled){
                var curTime = TrackingUtility.getFormattedStopwatch(it*1000L)
                val notification = curNotificationBuilder
                    .setContentText(curTime)

//            Timber.d("$it and ${curTime}")
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })

    }

    // Pause service
    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    /* Function to stop service*/
    private fun killService(){

        serviceKilled = true
        isFirstRun = true
        pauseService() // di puase dulu
        postInitValue() // balikin variabel ke nilai awal
        stopForeground(true)  // stop foreground service
        stopSelf() // stop service

    }

    private var isTimerEnabled = false
    private var timeRun = 0L // Total waktu berlari
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L


    // Start Timer
    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)

        var lapTime = 0L // selisih waktu
        
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){

                // Selisih waktu sekarang dan timer dimulai
                lapTime = System.currentTimeMillis() - timeStarted

                // Update time in milis -> timer in fragment
                timeRunInMillis.postValue(lapTime+timeRun)

                // update time in second -> update timer in notif
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){ // cek apakah udah lewat sedetik
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1L)
                    lastSecondTimeStamp += 1000L
                }

                delay(TIMER_DELAY) // update timer secara berkala, jadi gak setiap saat, user sih gak terlalu sadar
            }
            timeRun += lapTime
            Timber.d("Total waktu berlari : $timeRun")
        }
    }


    /*
    * Update Notification (terutama actionnya)
    * Saat State Berubah dari running ke gak running
    * dan vice versa
    * */
    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText =
            if (isTracking) "Pause" else "Resume"

        val pendingIntent = if (isTracking){
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)

        } else{
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 1, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)  as NotificationManager

        // Menghapus semua action yang ada
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        // Add new Action
        curNotificationBuilder = baseNotificationBuilder
            .addAction(R.drawable.ic_baseline_pause_24, notificationActionText, pendingIntent)


        if (!serviceKilled){
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }


    /*
    * Update Notification
    * membuat request notifikasi dari map
    * dipanggil ketika state IsRunning Berubah*/
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_UPDATE
                    priority = PRIORITY_HIGH_ACCURACY
                }

                // Request lokasi dari map
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback, // Callback
                    Looper.getMainLooper()
                )
            }
        } else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    /* Define callback when get location
    * Add new location to list of point*/
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value!!) {
                result?.locations?.let {
                    for (location in it) {
                        addPathPoint(location)
                        Timber.d("NEW Location ${location.latitude} ${location.longitude}")
                    }
                }
            }
        }

        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)
        }
    }

    // Menambahkan pathpoint ke livedata
    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    // Menambahkan pathpoints pada livedata
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }
        ?: pathPoints.postValue(mutableListOf(mutableListOf())) // Saat pathpoints null, maka buat list of list pertama


    // Create notification channel for android O above
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