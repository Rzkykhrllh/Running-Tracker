package com.example.runningtracking.other

import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
import java.sql.Time
import java.util.concurrent.TimeUnit

object TrackingUtility {

    /* Cek apakah app sudah dapat corase_locaction permission dan Fine_location
    * tambhahan di atas android Q Background_location karena by default dibawah Q udah auto akses*/
    fun hasLocationPermission(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else{
            EasyPermissions.hasPermissions(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    fun getFormattedStopwatch(ms : Long, includeMillis : Boolean = false) : String{
        var millisecond = ms

        var hours = TimeUnit.MILLISECONDS.toHours(millisecond)
        millisecond -= TimeUnit.HOURS.toMillis(hours) // mengupdate waktu yg tersisa

        var minutes = TimeUnit.MILLISECONDS.toMinutes(millisecond)
        millisecond -= TimeUnit.MINUTES.toMillis(minutes)

        var seconds = TimeUnit.MILLISECONDS.toSeconds(millisecond)
        millisecond -= TimeUnit.SECONDS.toMillis(seconds)
        millisecond /= 10

        var returnTime = "${if(hours<10) '0' else "" }$hours:" +
                "${if(minutes<10) '0' else "" }$minutes:" +
                "${if(seconds<10) '0' else "" }$seconds"

        if (!includeMillis) return returnTime
        else return returnTime+":${if(millisecond<10) '0' else "" }$millisecond"




    }
}