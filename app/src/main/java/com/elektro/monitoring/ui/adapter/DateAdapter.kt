package com.elektro.monitoring.ui.adapter

import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.elektro.monitoring.R
import com.elektro.monitoring.databinding.ItemSelectDateBinding
import com.elektro.monitoring.helper.sharedpref.SharedPrefData

class DateAdapter(private val itemList: List<String>, application: Application)
    : RecyclerView.Adapter<DateAdapter.ViewHolder>() {
    private val sharedPrefData = SharedPrefData(application)

    inner class ViewHolder(private val binding: ItemSelectDateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String, position: Int){
            binding.date.text = text
            binding.tanggal.setOnClickListener {
                sharedPrefData.editDataString("dateSelect", text)
                sharedPrefData.editDataString("dataRef", "$position/$text")
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
        holder.bind(currentItem, position)
    }
}