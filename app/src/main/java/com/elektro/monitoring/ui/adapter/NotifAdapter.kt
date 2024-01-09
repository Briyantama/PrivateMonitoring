package com.elektro.monitoring.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elektro.monitoring.databinding.ItemNotifBinding
import com.elektro.monitoring.helper.utils.showToast
import com.elektro.monitoring.model.NotifikasiSuhu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotifAdapter(private val itemList: List<NotifikasiSuhu>, private val context: Context)
    : RecyclerView.Adapter<NotifAdapter.ViewHolder>() {
    private val fireDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    inner class ViewHolder(private val binding: ItemNotifBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NotifikasiSuhu, context: Context){
            binding.tvPesanNotif.text = item.text
            binding.tvJenisNotif.text = item.title
            binding.ivClear.setOnClickListener {
                fireDatabase.getReference("notif").orderByChild("time")
                    .equalTo(item.time).addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (childSnapshot in snapshot.children) {
                                    childSnapshot.ref.removeValue()
                                        .addOnSuccessListener {
                                            context.showToast("Notifikasi ini berhasil dihapus")
                                        }.addOnFailureListener { e ->
                                            context.showToast("Gagal menghapus notifikasi \n${e.message}")
                                        }
                                }
                            }

                           override fun onCancelled(error: DatabaseError) {
                           }
                        })
            }
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
        holder.bind(currentItem, context)
    }
}