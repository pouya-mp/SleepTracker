package com.example.android.trackmysleepquality.sleeptracker

import androidx.recyclerview.widget.DiffUtil

class SleepNightDiffCallback : DiffUtil.ItemCallback<SleepNightAdapter.DataItem>() {

    override fun areItemsTheSame(oldItem: SleepNightAdapter.DataItem, newItem: SleepNightAdapter.DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SleepNightAdapter.DataItem, newItem: SleepNightAdapter.DataItem): Boolean {
        return oldItem == newItem
    }

}