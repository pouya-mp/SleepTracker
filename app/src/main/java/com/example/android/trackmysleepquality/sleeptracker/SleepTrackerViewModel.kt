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

import android.app.Application
import android.os.Handler
import android.util.Log
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*
import java.lang.Runnable

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application
) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var tonight = MutableLiveData<SleepNight?>()

    val nights = database.getAllNights()

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality


    val startButtonVisible = Transformations.map(tonight) {
        it == null
    }

    val stopButtonVisible = Transformations.map(tonight) {
        it != null
    }

    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    private val _recyclerViewVisibility = MutableLiveData(true)
    val recyclerViewVisibility: LiveData<Boolean>
        get() = _recyclerViewVisibility

    private val _showSnackbarEvent = MutableLiveData(false)
    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            return@withContext night
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)

            _navigateToSleepQuality.value = oldNight
        }
    }

    private var shouldClear = true
    private val _undoTimer = MutableLiveData(8)
    val undoTimer: LiveData<Int>
        get() = _undoTimer

    private var runnable: Runnable? = null

    fun onClearClicked() {
        shouldClear = true
        _showSnackbarEvent.value = true
        _recyclerViewVisibility.value = false
        var remainingTime = 9
        val handler = Handler()

        if (runnable == null) {
            runnable = Runnable {
                if (remainingTime > 0) {
                    remainingTime--
                    Log.i("remainingTime", "$remainingTime")
                    _undoTimer.postValue(remainingTime)
                    runnable?.let {
                        handler.postDelayed(it, 1000)
                    }

                } else if (remainingTime == 0 && shouldClear) {
                    onClear()
                } else {
                    //This never happens.
                }
            }
        }
        runnable?.let {
            handler.post(it)
        }


    }

    private fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
            doneHidingRecycleView()
        }
    }

    private val _currentRecycleLayout = MutableLiveData("linearLayout")
    val currentRecycleLayout: LiveData<String>
        get() = _currentRecycleLayout

    fun changeRecycleLayout() {
        if (_currentRecycleLayout.value.equals("linearLayout")) {
            changeToGridLayout()
        } else {
            changeToLinearLayout()
        }
    }

    private fun changeToGridLayout() {
        _currentRecycleLayout.postValue("gridLayout")
    }

    private fun changeToLinearLayout() {
        _currentRecycleLayout.postValue("linearLayout")
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    fun shouldUndo() {
        shouldClear = false
        doneHidingRecycleView()
    }

    private fun doneHidingRecycleView() {
        runnable = null
        _recyclerViewVisibility.value = true
    }
}

