package com.elektro.monitoring.ui.adapter

import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.elektro.monitoring.R
import com.elektro.monitoring.databinding.ItemSelectDateBinding
import com.elektro.monitoring.helper.Constants.TAG
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.helper.utils.getMonth
import com.elektro.monitoring.helper.utils.getTanggal
import com.elektro.monitoring.helper.utils.getWeekNumber
import com.elektro.monitoring.helper.utils.invertBulan
import com.elektro.monitoring.helper.utils.invertMinggu

class DateAdapter(private val itemList: List<String>, application: Application)
    : RecyclerView.Adapter<DateAdapter.ViewHolder>() {
    private val sharedPrefData = SharedPrefData(application)

    inner class ViewHolder(private val binding: ItemSelectDateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String){
            binding.date.text = text
            binding.tanggal.setOnClickListener {
                val period = sharedPrefData.callDataString("period")
                sharedPrefData.editDataString("dateSelect", text)
                saveSP(period, text)
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_dateSelectFragment_to_dataShowFragment)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectDateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.bind(currentItem)
    }

    private fun saveSP(period: String, text: String){
        when (period) {
            "Monthly" -> {
                val bulan = invertBulan(text)

                sharedPrefData.editDataString("dateRef", bulan)
            }
            "Weekly" -> {
                val part = text.split(" ")
                val minggu = invertMinggu(part[0] + " " + part[1])
                val bulan = invertBulan(part[2])

                sharedPrefData.editDataString("dateRef", "$bulan/$minggu")
            }
            "Daily" -> {
                val week = getWeekNumber(text)
                val tanggal = getTanggal(text)
                val bulan = invertBulan(getMonth(text))
                Log.d(TAG, "saveSP: $week\n$tanggal\n$bulan\n$text")

                sharedPrefData.editDataString("dateRef", "$bulan/$week/$tanggal/$text")
            }
        }
    }
}