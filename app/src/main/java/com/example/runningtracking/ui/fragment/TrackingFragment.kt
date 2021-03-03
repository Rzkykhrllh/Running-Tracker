package com.example.runningtracking.ui.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.runningtracking.R
import com.example.runningtracking.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel : MainViewModel by viewModels()

}