package com.example.runningtracking.db

import androidx.lifecycle.LiveData
import androidx.room.*

/* DAO adalah blueprint untuk aksi apa saja yang dapat dilakukan terhadap sebuah tabel*/
@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("select * from running_table order by timeStamp desc")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>


    @Query("select * from running_table order by timeInMS desc")
    fun getAllRunsSortedByDuration(): LiveData<List<Run>>

    @Query("select * from running_table order by avgSpeedInKMH desc")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>


    @Query("select * from running_table order by distanceInMeters desc")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>


    @Query("select * from running_table order by caloriesBurned desc")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>


    @Query("Select sum(timeInMS) from running_table")
    fun getTotalDuration(): LiveData<Long>


    @Query("Select sum(caloriesBurned) from running_table")
    fun getTotalCaloriesBurned(): LiveData<Int>


    @Query("Select sum(distanceInMeters) from running_table")
    fun getTotalDistance(): LiveData<Int>


    @Query("Select avg(avgSpeedInKMH) from running_table")
    fun getTotalAvgSpeed(): LiveData<Float>

}