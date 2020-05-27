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

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    private lateinit var binding: FragmentSleepTrackerBinding

    private lateinit var sleepTrackerViewModel: SleepTrackerViewModel

    private val adapter = SleepNightAdapter()

    private var snackbar: Snackbar? = null

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false
        )

        val application = requireNotNull(this.activity).application

        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao

        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)
        sleepTrackerViewModel =
                ViewModelProvider(this, viewModelFactory).get(SleepTrackerViewModel::class.java)

        binding.sleepTrackerViewModel = sleepTrackerViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        sleepTrackerViewModel.navigateToSleepQuality.observe(viewLifecycleOwner, Observer { night ->
            night?.let {
                findNavController().navigate(
                        SleepTrackerFragmentDirections.actionSleepTrackerFragmentToSleepQualityFragment(
                                it.nightId
                        )
                )
                sleepTrackerViewModel.doneNavigating()
            }
        })
        sleepTrackerViewModel.recyclerViewVisibility.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.recyclerView.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.GONE
            }
        })

        sleepTrackerViewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                snackbar = Snackbar.make(
                        binding.rootLayout,
                        getString(R.string.cleared_message),
                        8000
                )

                snackbar?.show()

                sleepTrackerViewModel.doneShowingSnackbar()
            }
        })

        sleepTrackerViewModel.undoTimer.observe(viewLifecycleOwner, Observer {
            snackbar?.setAction(getString(R.string.undo, it), this::undo)
        })

        return binding.root
    }

    private fun undo(view: View) {
        sleepTrackerViewModel.shouldUndo()
        snackbar = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = adapter
        sleepTrackerViewModel.nights.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it ?: emptyList())
        })
        sleepTrackerViewModel.currentRecycleLayout.observe(viewLifecycleOwner, Observer {
            if (it == "gridLayout") {
                binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
                binding.changeLayoutButton.setImageResource(R.drawable.ic_linear_layout)
            } else {
                binding.recyclerView.layoutManager = LinearLayoutManager(context)
                binding.changeLayoutButton.setImageResource(R.drawable.ic_grid_layout)
            }
        })
    }
}
