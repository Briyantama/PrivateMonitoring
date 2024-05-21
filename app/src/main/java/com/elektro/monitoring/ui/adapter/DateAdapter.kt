package com.elektro.monitoring.ui.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.elektro.monitoring.R
import com.elektro.monitoring.databinding.ItemSelectDateBinding
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
        fun bind(text: String) {
            binding.date.text = text
            binding.tanggal.setOnClickListener {
                val period = sharedPrefData.callDataString("period")
                sharedPrefData.editDataString("dateSelect", text)
                saveSP(period, text)
                navigateToDataShowFragment()
            }
        }

        private fun navigateToDataShowFragment() {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_dateSelectFragment_to_dataShowFragment)
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

    private fun saveSP(period: String, text: String) {
        val dateRef = when (period) {
            "Monthly" -> invertBulan(text)
            "Weekly" -> {
                val (week, month) = invertWeekAndMonth(text)
                "$month/$week"
            }
            "Daily" -> {
                val (week, day, month, year) = invertWeekDayMonthYear(text)
                "$month/$week/$day/$year"
            }
            else -> ""
        }

        sharedPrefData.editDataString("dateRef", dateRef)
    }

    private fun invertWeekAndMonth(text: String): Pair<String, String> {
        val parts = text.split(" ")
        val week = invertMinggu("${parts[0]} ${parts[1]}")
        val month = invertBulan(parts[2])
        return Pair(week, month)
    }

    private fun invertWeekDayMonthYear(text: String): List<String> {
        val week = getWeekNumber(text).toString()
        val day = getTanggal(text)
        val month = invertBulan(getMonth(text))
        val year = text.substringAfterLast(' ')
        return listOf(week, day, month, year)
    }
}
