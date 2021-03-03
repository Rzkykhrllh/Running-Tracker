package com.example.runningtracking.ui.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runningtracking.R
import com.example.runningtracking.ui.viewmodel.StatisticViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticFragment : Fragment(R.layout.fragment_statistic){

    private val viewModel : StatisticViewModel by viewModels()
}