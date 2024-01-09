package com.elektro.monitoring.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elektro.monitoring.databinding.FragmentNotificationBinding
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.helper.utils.dialogPN
import com.elektro.monitoring.helper.utils.showToast
import com.elektro.monitoring.model.NotifikasiSuhu
import com.elektro.monitoring.ui.adapter.NotifAdapter
import com.elektro.monitoring.viewmodel.DataViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationFragment : Fragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPrefData: SharedPrefData

    private val dataViewModel: DataViewModel by viewModels()
    private val fireDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivClearAll.setOnClickListener {
            requireContext().dialogPN("Hapus Semua Notifikasi", "Yakin?", "Iya", "Tidak") {
                fireDatabase.getReference("notif").removeValue()
                    .addOnSuccessListener {
                        sharedPrefData.editDataInt("sizeNotif", 0)
                        requireContext().showToast("Semua notifikasi berhasil dihapus")
                    }.addOnFailureListener { e ->
                        requireContext().showToast("Gagal menghapus notifikasi \n${e.message}")
                    }
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    override fun onResume() {
        super.onResume()
        fireDatabase.getReference("notif").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listNotifikasiSuhu: MutableList<NotifikasiSuhu> = mutableListOf()

                    snapshot.children.forEach { value ->
                        val newData = value.getValue(NotifikasiSuhu::class.java)
                        newData?.let { listNotifikasiSuhu.add(it) }
                    }

                    dataViewModel.listNotif.postValue(listNotifikasiSuhu)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        dataViewModel.listNotif.observe(viewLifecycleOwner){
            showHomeProductList(it, requireContext())
        }
    }
    private fun showHomeProductList(listNotifikasiSuhu: MutableList<NotifikasiSuhu>, context: Context) {
        val adapter = NotifAdapter(listNotifikasiSuhu, requireContext())
        binding.rvNotif.adapter = adapter
        binding.rvNotif.layoutManager = LinearLayoutManager(context)
    }
}