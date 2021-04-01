package com.example.runningtracking.repository

import com.example.runningtracking.db.Run
import com.example.runningtracking.db.RunDAO
import javax.inject.Inject

// Tugas repository adalah mengumpulkan data dari semua source
class   MainRepository @Inject constructor(
    val runDao : RunDAO
) {

    suspend fun insertRun(run : Run) = runDao.insertRun(run)

    suspend fun deleteRun(run : Run) = runDao.deleteRun(run)

    fun getAllRunSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunSortedByDuration() = runDao.getAllRunsSortedByDuration()

    fun getAllRunSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalDuration() = runDao.getTotalDuration()







}