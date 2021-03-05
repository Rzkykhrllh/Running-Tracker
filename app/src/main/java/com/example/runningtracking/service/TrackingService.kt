package com.example.runningtracking.service

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.example.runningtracking.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracking.other.Constants.ACTION_STOP_SERVICE
import timber.log.Timber

/* Karena bakal ngeobserve live-data
* dan live data hanya bisa di observe di dengan fungsi observe
* dan fungsi observe membutuhkan lige cycle owner*/

class TrackingService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE ->{
                    Timber.d("Started or resumed the service")
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
}