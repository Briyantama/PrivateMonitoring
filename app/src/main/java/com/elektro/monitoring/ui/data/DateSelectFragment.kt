package com.elektro.monitoring.ui.data

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elektro.monitoring.R
import com.elektro.monitoring.databinding.FragmentDateSelectBinding
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.ui.adapter.DateAdapter
import com.elektro.monitoring.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DateSelectFragment : Fragment() {
    private var _binding: FragmentDateSelectBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPrefData: SharedPrefData
    private val dataViewModel: DataViewModel by viewModels()

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

        val stringArrayList: ArrayList<String> = ArrayList()
        val periodArray: Array<String> = resources.getStringArray(R.array.period_array)
        stringArrayList.addAll(periodArray)

        binding.spinnerDateSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sharedPrefData.editDataString("period", stringArrayList[position])
                dataViewModel.dateSelect(stringArrayList[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

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