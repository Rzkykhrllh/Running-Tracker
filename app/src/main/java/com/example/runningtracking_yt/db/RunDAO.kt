package com.example.runningtracking_yt.db

import androidx.lifecycle.LiveData
import androidx.room.*

/* DAO adalah blueprint untuk aksi apa saja yang dapat dilakukan terhadap sebuah tabel*/
@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run : Run)

    @Delete
    suspend fun deleteRun(run : Run)

    @Query("select * from running_table order by timeStamp desc ")
    suspend fun getAllRunsSortedByDate() : LiveData<List<Run>>


    @Query("select * from running_table order by timeInMS desc ")
    suspend fun getAllRunsSortedByDuration() : LiveData<List<Run>>


    @Query("select * from running_table order by avgSpeedInKMH desc ")
    suspend fun getAllRunsSortedByAvgSpeed() : LiveData<List<Run>>


    @Query("select * from running_table order by distanceInMeters desc ")
    suspend fun getAllRunsSortedByDistance() : LiveData<List<Run>>


    @Query("select * from running_table order by caloriesBurned desc ")
    suspend fun getAllRunsSortedByCaloriesBurned() : LiveData<List<Run>>


    @Query("Select sum(timeInMS) from running_table")
    suspend fun getTotalDuration() : LiveData<Long>


    @Query("Select sum(caloriesBurned) from running_table")
    suspend fun getTotalCaloriesBurned() : LiveData<Int>


    @Query("Select sum(distanceInMeters) from running_table")
    suspend fun getTotalDistance() : LiveData<Int>


    @Query("Select avg(avgSpeedInKMH) from running_table")
    suspend fun getTotalAvgSpeed() : LiveData<Float>

}