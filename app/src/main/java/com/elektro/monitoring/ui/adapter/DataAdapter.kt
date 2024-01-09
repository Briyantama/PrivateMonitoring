package com.elektro.monitoring.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elektro.monitoring.databinding.ItemDataShowBinding
import com.elektro.monitoring.model.Data10Min

class DataAdapter(private val itemList: List<Data10Min>)
    : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDataShowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Data10Min){
            binding.dataCurrentIn.text = data.arusMasuk.toString()
            binding.dataCurrentOut.text = data.arusKeluar.toString()
            binding.dataTegangan.text = data.tegangan.toString()
            binding.dataSoc.text = data.soc.toString()
            binding.dataTime.text = data.currentTime
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapter.ViewHolder {
        val binding = ItemDataShowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.bind(currentItem)
    }

}