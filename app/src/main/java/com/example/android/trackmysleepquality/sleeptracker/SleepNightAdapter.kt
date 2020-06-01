package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import com.example.android.trackmysleepquality.inflateFromBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SleepNightAdapter(private var onClickListener: OnClickListener) : ListAdapter<SleepNightAdapter.DataItem, RecyclerView.ViewHolder>(SleepNightDiffCallback()) {

    companion object {
        private const val ITEM_VIEW_TYPE_HEADER = 0
        private const val ITEM_VIEW_TYPE_ITEM = 1
    }

    interface OnClickListener {
        fun onClick(sleepNight: SleepNight)
    }

    class ItemViewHolder(private val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                return ItemViewHolder(parent.inflateFromBinding(R.layout.list_item_sleep_night) as ListItemSleepNightBinding)
            }
        }

        fun bind(item: SleepNight, listener: OnClickListener) {
            binding.sleep = item
            binding.clickListener = listener
            binding.executePendingBindings()
        }
    }

    class TextViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.viewholder_text_item, parent, false) as TextView
                return TextViewHolder(view)
            }
        }
    }

    sealed class DataItem {
        data class SleepNightItem(val sleepNight: SleepNight) : DataItem() {
            override val id = sleepNight.nightId
        }

        object Header : DataItem() {
            override val id = Long.MIN_VALUE
        }

        abstract val id: Long
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ItemViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder ->
                holder.bind((getItem(position) as DataItem.SleepNightItem).sleepNight, onClickListener)
            is TextViewHolder ->
                holder.textView.text = "TITLE"
        }
    }

    fun addHeaderAndSubmitList(list: List<SleepNight>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
            }

            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

}