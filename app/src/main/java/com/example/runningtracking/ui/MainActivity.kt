package com.example.runningtracking.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.runningtracking.R
import com.example.runningtracking.databinding.ActivityMainBinding
import com.example.runningtracking.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* Ketika mainactivity di (re)create
        * cek apakah yg manggil itu service, kalau iya navigate ke tracking fragment*/
        navigateToTrackingFragmentIfNeeded(intent)

        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        val navController = findNavController(R.id.navHostFragment)
        // Passing each menu ID as a set of Ids because each

      /*  val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_order, R.id.navigation_profile
            )
        )*/

        // menu should be considered as top level destinations.
        navView.setupWithNavController(navController)


    }

    /* Apabila activity gak dibuat baru, maka cek intentnya juga*/
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent : Intent?){

        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            findNavController(R.id.navHostFragment).navigate(R.id.action_globar_tracking_fragment)
        }

    }




}