package com.example.runningtracking.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtracking.db.Run
import com.example.runningtracking.other.SortType
import com.example.runningtracking.repository.MainRepository
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
    /*Main repo udah auto generated sama dagger walaupun kita gak deklarasiin di AppModule
    karena di mainRepo sendiri udah make injection buat manggil DAO
    Jadi si Dagger-Hilt auto tau juga cara provide mainRepo*/
) : ViewModel() {

    /* Live data from database */
    val runSortedByDate = mainRepository.getAllRunSortedByDate()
    val runSortedByDistance = mainRepository.getAllRunSortedByDistance()
    val runSortedByCalories = mainRepository.getAllRunSortedByCaloriesBurned()
    val runSortedByDuration = mainRepository.getAllRunSortedByDuration()
    val runSortedByAvgspeed = mainRepository.getAllRunSortedByAvgSpeed()

    /*Custom live data, biar bisa di sort
    * ngeobser live data lainnya */
    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    /* Initial assignment to runs mediator*/
    init {

        // Assign value to runs for the first time
        runs.addSource(runSortedByDate){
            if (sortType == SortType.DATE){
                it?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortedByDistance){
            if (sortType == SortType.DISTANCE){
                it?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortedByDuration){
            if (sortType == SortType.DURATION){
                it?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortedByAvgspeed){
            if (sortType == SortType.AVG_SPEED){
                it?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortedByCalories){
            if (sortType == SortType.CALORIES){
                it?.let {
                    runs.value = it
                }
            }
        }
    }

    /* Update RV ketika sortType diganti ?*/
    fun sortRuns(sortType: SortType) =

        // sebenarnya cuma assign nilai runSortedBy ke runs
        when (sortType){
            SortType.DISTANCE -> runSortedByDistance.value?.let { runs.value = it }
            SortType.DURATION -> runSortedByDuration.value?.let { runs.value = it }
            SortType.AVG_SPEED -> runSortedByAvgspeed.value?.let { runs.value = it }
            SortType.CALORIES -> runSortedByCalories.value?.let { runs.value = it }
            else ->  runSortedByDate.value?.let { runs.value = it }
        }.also {
            this.sortType = sortType
        }



    /* Add run to database*/
    fun insertRun(run  : Run){
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }
    }
}