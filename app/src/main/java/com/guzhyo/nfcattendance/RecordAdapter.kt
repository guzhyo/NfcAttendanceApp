package com.guzhyo.nfcattendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.guzhyo.nfcattendance.databinding.ItemRecordBinding

class RecordAdapter(private val items: List<String>) :
    RecyclerView.Adapter<RecordAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val parts = items[position].split("|")
        holder.binding.timestampText.text = parts[0]
        holder.binding.indexText.text = "${items.size - position}"
        if (parts.size > 1) {
            holder.binding.apduText.text = parts[1]
        } else {
            holder.binding.apduText.text = ""
        }
    }

    override fun getItemCount() = items.size
}
