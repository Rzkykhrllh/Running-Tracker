package com.example.runningtracking.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningtracking.R
import com.example.runningtracking.databinding.FragmentSetupBinding
import com.example.runningtracking.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningtracking.other.Constants.KEY_FOR_NAME
import com.example.runningtracking.other.Constants.KEY_FOR_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetUpFragment : Fragment() {

    lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPreference: SharedPreferences

    @set:Inject // karena primitive jadi harus make set, gak bisa langsung inject
    var isFirstTime = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstTime){
            /* Remove setup fragment from backstack*/
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setUpFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setUpFragment_to_runFragment2,
                savedInstanceState,
                navOptions
            )
        }

        binding.tvContinue.setOnClickListener {

            val success = savePersonalDataToSharedPreferences()


            if (success){
                findNavController().navigate(R.id.action_setUpFragment_to_runFragment2)
            } else{
                Snackbar.make(requireView(), "Please enter all fields", Snackbar.LENGTH_SHORT).show()
            }

        }
    }

    /* Save data to shared pereference*/
    private fun savePersonalDataToSharedPreferences(): Boolean {

        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()

        if (name.isNullOrEmpty() || weight.isNullOrEmpty())
            return false

        sharedPreference.edit()
            .putString(KEY_FOR_NAME, name)
            .putFloat(KEY_FOR_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()

        val toolbarText = "Let's go $name"
    //      requireActivity().

        return true

    }

}