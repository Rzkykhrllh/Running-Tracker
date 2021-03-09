package com.example.runningtracking.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningtracking.R
import com.example.runningtracking.databinding.FragmentTrackingBinding
import com.example.runningtracking.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracking.other.Constants.MAP_ZOOM
import com.example.runningtracking.other.Constants.POLYLINE_COLOR
import com.example.runningtracking.other.Constants.POLYLINE_WIDTH
import com.example.runningtracking.other.TrackingUtility
import com.example.runningtracking.service.TrackingService
import com.example.runningtracking.service.polyline
import com.example.runningtracking.service.polylines
import com.example.runningtracking.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    lateinit var binding: FragmentTrackingBinding

    private var map: GoogleMap? = null // Deklarasi objek map, buat mapView

    private var isTracking = false
    private var pathPoints = mutableListOf<polyline>()
    
    private var curTimeinMilli = 0L


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState) // Create map

        /* Dipanggil ketika fragment dibuat */
        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        subscribeToObservers()
    }


    // Start Tracking Service
    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    // Observe TrackingService Live Data
    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {

            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })
        
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer{
            curTimeinMilli = it

            var formattdTime = TrackingUtility.getFormattedStopwatch(curTimeinMilli, true)
            binding.tvTimer.setText(formattdTime)
        })

    }

    /* Logic for button */
    private fun toggleRun(){
        if (isTracking){
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    /* Change button attribute */
    private fun updateTracking(isTracking : Boolean){
        this.isTracking = isTracking
        if (!isTracking){
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else{
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE

        }

    }

    /* Memindhkan kamera ke titik terakhir*/
    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() &&  pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    /* Menggambar garis dengan semua titik*/
    private fun addAllPolylines(){
        for (polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }

    }

    /* Menggambar line pada titik  terbaru*/
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size-2] // Mengambil kordinat sebelum terakhir
            val lastLatLng = pathPoints.last().last()

            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions) // gambar polyline di map



        }
    }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView?.onSaveInstanceState(outState) // Buat caching, biar gak ngulang dari awal load map nya
    }


}