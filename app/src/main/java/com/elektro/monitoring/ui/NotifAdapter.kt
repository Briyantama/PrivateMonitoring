package com.elektro.monitoring.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elektro.monitoring.databinding.ItemNotifBinding
import com.elektro.monitoring.model.NotifikasiSuhu

class NotifAdapter(private val itemList: List<NotifikasiSuhu>)
    : RecyclerView.Adapter<NotifAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemNotifBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotifikasiSuhu){
            binding.tvPesanNotif.text = item.text
            binding.tvJenisNotif.text = item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotifBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.bind(currentItem)
    }
}