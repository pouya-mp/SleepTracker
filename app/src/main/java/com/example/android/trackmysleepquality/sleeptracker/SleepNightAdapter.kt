package com.example.android.trackmysleepquality.sleeptracker

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import com.example.android.trackmysleepquality.inflateFromBinding

class SleepNightAdapter : ListAdapter<SleepNight, SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()) {

    class ViewHolder(private val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(parent.inflateFromBinding(R.layout.list_item_sleep_night) as ListItemSleepNightBinding)
            }
        }

        fun bind(item: SleepNight) {
            binding.sleep = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}