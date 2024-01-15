package com.elektro.monitoring.ui.data

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elektro.monitoring.databinding.FragmentDateSelectBinding
import com.elektro.monitoring.helper.Constants.TAG
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.ui.adapter.DateAdapter
import com.elektro.monitoring.viewmodel.DataViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DateSelectFragment : Fragment() {
    private var _binding: FragmentDateSelectBinding? = null
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
        _binding = FragmentDateSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    override fun onResume() {
        super.onResume()
        val selectedPanel =  sharedPrefData.callDataString("selectedpanel")

        fireDatabase.getReference("panels").child(selectedPanel)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listDate: MutableList<String> = mutableListOf()

                    snapshot.children.forEach{ value ->
                        value.children.forEach { nilai ->
                            val newData = nilai.key.toString()
                            listDate.add(newData)
                        }
                    }

                    dataViewModel.listDate.postValue(listDate)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        dataViewModel.listDate.observe(viewLifecycleOwner){
            showListDate(it, requireContext(), requireActivity().application)
        }
    }

    private fun showListDate(listDate: MutableList<String>, context: Context, application: Application) {
        val adapter = DateAdapter(listDate, application)
        binding.rvSelectDate.adapter = adapter
        binding.rvSelectDate.layoutManager = LinearLayoutManager(context)
    }
}