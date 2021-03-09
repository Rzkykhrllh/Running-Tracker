package com.example.runningtracking.other

import android.graphics.Color
import com.google.android.gms.maps.model.Polyline

object Constants {

    const val RUNNING_DATABASE_NAME = "running_db"

    const val REQUEST_CODE_LOCATION_PERMISSIONS = 0


    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT" // konstanta untuk pending intent, sehingga pergi ke tracking fragment

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_UPDATE = 2000L

    const val POLYLINE_COLOR = Color.CYAN
    const val POLYLINE_WIDTH = 8F
    const val MAP_ZOOM = 20F

    const val CHANNEL_ID = "tracking_channel"
    const val CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val TIMER_DELAY = 50L

}