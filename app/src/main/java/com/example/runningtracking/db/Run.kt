package com.example.runningtracking.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table") // Nama tabel yang menggunakan Run sebagai strukturnya
data class Run(
    var img : Bitmap? = null,
    var timeStamp : Long = 0L, // Tanggal
    var avgSpeedInKMH : Float = 0f,
    var distanceInMeters : Int = 0,
    var timeInMS : Long = 0L, // Berama lama berlari
    var caloriesBurned : Int = 0
){
    @PrimaryKey(autoGenerate = true)
    var id : Int? = null
}
