package com.example.runningtracking.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtracking.db.Run
import com.example.runningtracking.repository.MainRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
    /*Main repo udah auto generated sama dagger walaupun kita gak deklarasiin di AppModule
    karena di mainRepo sendiri udah make injection buat manggil DAO
    Jadi si Dagger-Hilt auto tau juga cara provide mainRepo*/
) : ViewModel() {

    /* Add run to database*/
    fun insertRun(run  : Run){
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }
    }

}