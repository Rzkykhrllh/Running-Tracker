package com.example.runningtracking.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningtracking.R
import com.example.runningtracking.adapter.RunAdapter
import com.example.runningtracking.databinding.FragmentTrackingBinding
import com.example.runningtracking.db.Run
import com.example.runningtracking.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracking.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracking.other.Constants.ACTION_STOP_SERVICE
import com.example.runningtracking.other.Constants.MAP_ZOOM
import com.example.runningtracking.other.Constants.POLYLINE_COLOR
import com.example.runningtracking.other.Constants.POLYLINE_WIDTH
import com.example.runningtracking.other.TrackingUtility
import com.example.runningtracking.service.TrackingService
import com.example.runningtracking.service.polyline
import com.example.runningtracking.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    lateinit var binding: FragmentTrackingBinding

    private var map: GoogleMap? = null // Deklarasi objek map, buat mapView

    private var isTracking = false
    private var pathPoints = mutableListOf<polyline>()

    private var curTimeinMilli = 0L

    private var menu: Menu? = null

    private var wieght = 80f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(layoutInflater)
        this.setHasOptionsMenu(true) // Fragmeent ini punya option menu
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

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        subscribeToObservers()
    }

    /* Logic for button */
    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }


    // Start Tracking Service
    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    // Observe TrackingService Live Data
    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {

            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeinMilli = it

            var formattdTime = TrackingUtility.getFormattedStopwatch(curTimeinMilli, true)
            binding.tvTimer.setText(formattdTime)
        })

    }




    /* Attach option menu to (custom) toolbar*/
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu

        Timber.d("Option Menu-created $menu")
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        Timber.d("Menu-prepared $menu")

        /* Menu tampil jika waktu lebih dari 1 detik */
        if (curTimeinMilli > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    /* Fungsi ketika menu dipilih*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.miCancelTracking ->
                showCancelAlertDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    /* Show Alert dialog when stop menu clicked */
    private fun showCancelAlertDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel the Run")
            .setMessage("Are you sure to cancel the current run and delete all its data ?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.cancel() // cancel the dialog
            }
            .create()

        dialog.show()
    }

    /* Function to cancel the tracking*/
    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment2)
    }


    /* Change button attribute */
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE
            menu?.getItem(0)?.isVisible = true

        }

    }

    /* Memindhkan kamera ke titik terakhir*/
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    /* Menggambar garis dengan semua titik*/
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }

    }

    /* Zoom out to see the whole track*/
    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.builder()

        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    /* save run to db and move to home*/
    private fun endRunAndSaveToDb() {

        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            var avgSpeed = (distanceInMeters / 1000f) / (curTimeinMilli / 1000f / 60 / 60)
            avgSpeed = round(avgSpeed * 10) / 10f // 1 tempat desimal

            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters * wieght) / 1000f).toInt()

            val run = Run(bmp,dateTimeStamp, avgSpeed, distanceInMeters, curTimeinMilli, caloriesBurned)
            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView), // karena habis simpan kita langsung keluar dari tracking, jadi kudu view main activitinya
                "Run Saved",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        stopRun()

    }

    /* Menggambar line pada titik  terbaru*/
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng =
                pathPoints.last()[pathPoints.last().size - 2] // Mengambil kordinat sebelum terakhir
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