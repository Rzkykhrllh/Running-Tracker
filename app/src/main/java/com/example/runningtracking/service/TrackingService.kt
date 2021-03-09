package com.example.runningtracking.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


/* Jadi garis adalah kumpulan titik-titik
* dan kita akan memiliki kumpulan garis*/
typealias polyline = MutableList<LatLng>
typealias polylines = MutableList<polyline>


/* Karena bakal ngeobserve live-data
* dan live data hanya bisa di observe di dengan fungsi observe
* dan fungsi observe membutuhkan lige cycle owner*/
class TrackingService : LifecycleService() {

    private var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient // Objek Buat request lokasi

    private var timeRunInSeconds = MutableLiveData<Long>() // Waktu buat update notfikasi


    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<polylines>() // Live data of list of line

        var timeRunInMillis = MutableLiveData<Long>() // Waktu untuk ditampilkan di halaman Tracking


    }


    // Init value awal dari livedata
    private fun postInitValue() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("Service-desu onCreate")

        postInitValue()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        // Observe isTracking
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Service-desu onStartCommand")
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or resumed the service")

                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("resumed the service")
                        startTimer()

                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Pause Service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop Service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                // Selisih waktu sekarang dan timer dimulau
                lapTime = System.currentTimeMillis() - timeStarted

                // Update live data value
                timeRunInMillis.postValue(lapTime)

                // update time in second
                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){ // cek apakah udah lewat sedetik
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1L)
                    lastSecondTimeStamp = 1000L
                }

                delay(TIMER_DELAY) // update timer secara berkala, jadi gak setiap saat, user sih gak terlalu sadar
            }
            timeRun += lapTime
        }
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }


    // Update location
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_UPDATE
                    priority = PRIORITY_HIGH_ACCURACY
                }

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

    // get new location and add it to live data
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

    // Menambahkan pathpoints pada
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }
        ?: pathPoints.postValue(mutableListOf(mutableListOf())) // Saat pathpoints null, maka buat list of list pertama


    // Start service
    private fun startForegroundService() {

        startTimer()
        isTracking.postValue(true)

        //val notificationManager = NotificationManagerCompat.from(this)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    // make public intennt for notification
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