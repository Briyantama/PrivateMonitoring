package com.elektro.monitoring.ui.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elektro.monitoring.databinding.ItemDataShowBinding
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.model.Data10Min

class DataAdapter(private val itemList: List<Data10Min>, private val mdateList: List<String>?, application: Application)
    : RecyclerView.Adapter<DataAdapter.ViewHolder>() {
    private val sharedPrefData = SharedPrefData(application)

    inner class ViewHolder(private val binding: ItemDataShowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Data10Min, dateList: List<String>?, position: Int){
            val period = sharedPrefData.callDataString("period")
            val arusMasuk = "${data.arusMasuk}A"
            val arusKeluar = "${data.arusKeluar}A"
            val tegangan = "${data.tegangan}v"
            val soc = "${data.soc}%"

            val date = when (period) {
                "Monthly", "Weekly" -> dateList?.getOrNull(position) ?: ""
                else -> ""
            }

            binding.dataTime.text = if (date.isNotEmpty()) "$date ${data.currentTime}" else data.currentTime
            binding.dataSoc.text = soc
            binding.dataTegangan.text = tegangan
            binding.dataCurrentIn.text = arusMasuk
            binding.dataCurrentOut.text = arusKeluar
            binding.dataNo.text = position.plus(1).toString()
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
        holder.bind(currentItem, mdateList, position)
    }
}