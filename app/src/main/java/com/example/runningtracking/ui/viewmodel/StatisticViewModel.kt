package com.example.runningtracking.ui.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.runningtracking.repository.MainRepository

class StatisticViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
}