/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleepquality

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepQualityBinding

/**
 * Fragment that displays a list of clickable icons,
 * each representing a sleep quality rating.
 * Once the user taps an icon, the quality is set in the current sleepNight
 * and the database is updated.
 */
class SleepQualityFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */

    val arguments: SleepQualityFragmentArgs by navArgs()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding = FragmentSleepQualityBinding.inflate(inflater, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao
        val viewModelFactory = SleepQualityViewModelFactory(arguments.sleepNightKey, dataSource)
        val viewModel =
                ViewModelProvider(this, viewModelFactory).get(SleepQualityViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.sleepQualityViewModel = viewModel

        viewModel.navigateToSleepTracker.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                findNavController().navigate(SleepQualityFragmentDirections.actionSleepQualityFragmentToSleepTrackerFragment())
                viewModel.doneNavigating()
            }
        })

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            context?.let {
                val alertDialog = AlertDialog.Builder(it)
                alertDialog.setTitle(getString(R.string.confirm_going_back_title))
                alertDialog.setMessage(getString(R.string.confirm_going_back_description))
                alertDialog.setPositiveButton("yes") { _, _ -> findNavController().popBackStack() }
                alertDialog.setNeutralButton("cancel") { _, _ -> }
                alertDialog.show()
            }

        }



        return binding.root
    }
}
