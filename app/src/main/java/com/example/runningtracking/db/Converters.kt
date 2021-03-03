package com.example.runningtracking.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    /* Konversi bitmap ke bytearray
    *  Database simpan dalam bentuk ByteArray*/
    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()

        /* Konvert bitmap ke byte array
        * 1. format bitmap -> png
        * 2. kualitas 100%
        * 3. simpan kemana*/
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        return outputStream.toByteArray()
    }

    /* Konversi bitmap ke bytearray
    *  Database simpan dalam bentuk ByteArray*/
    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)


}