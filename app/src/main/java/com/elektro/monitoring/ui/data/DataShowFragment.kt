package com.elektro.monitoring.ui.data

import android.R
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.elektro.monitoring.databinding.FragmentDataShowBinding
import com.elektro.monitoring.helper.Constants.TAG
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.model.Data10Min
import com.elektro.monitoring.ui.adapter.DataAdapter
import com.elektro.monitoring.viewmodel.DataViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DataShowFragment : Fragment() {
    private var _binding: FragmentDataShowBinding? = null
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
        _binding = FragmentDataShowBinding.inflate(inflater, container, false)
        val dateselect = sharedPrefData.callDataString("dateSelect")

        binding.horizontalScroll.isHorizontalScrollBarEnabled = false
        binding.verticalScroll.isVerticalScrollBarEnabled = false
        binding.tableData.dividerDrawable = resources.getDrawable(R.drawable.divider_horizontal_bright)
        binding.tableData.showDividers = TableLayout.SHOW_DIVIDER_BEGINNING or
                TableLayout.SHOW_DIVIDER_MIDDLE or TableLayout.SHOW_DIVIDER_END

        binding.tvDate.text = dateselect

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
        val ref = sharedPrefData.callDataString("dataRef")

        fireDatabase.getReference(ref).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listData: MutableList<Data10Min> = mutableListOf()

                snapshot.children.forEach{ value ->
                    val newData = value.getValue(Data10Min::class.java)
                    newData?.let { listData.add(it) }
                    Log.d(TAG, "Value: $newData")
                }

                dataViewModel.mdata10MinList.postValue(listData)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        dataViewModel.data10MinList.observe(viewLifecycleOwner){
            showListData(it, requireContext())
        }
    }

    private fun showListData(listDate: MutableList<Data10Min>, context: Context) {
        val adapter = DataAdapter(listDate)
        binding.rvDataShow.adapter = adapter
        binding.rvDataShow.layoutManager = LinearLayoutManager(context)
    }
}