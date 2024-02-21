package com.elektro.monitoring.ui.data

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elektro.monitoring.databinding.FragmentDataShowBinding
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.helper.utils.settingLineChart
import com.elektro.monitoring.helper.utils.updateLineChart
import com.elektro.monitoring.model.Data10Min
import com.elektro.monitoring.ui.adapter.DataAdapter
import com.elektro.monitoring.viewmodel.DataViewModel
import com.github.mikephil.charting.data.Entry
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

    private val mCurrentIn: MutableList<Entry> = mutableListOf()
    private val mCurrentOut: MutableList<Entry> = mutableListOf()
    private val mTime: MutableList<String> = mutableListOf()
    private val mTegangan: MutableList<Entry> = mutableListOf()
    private val mSoC: MutableList<Entry> = mutableListOf()

    private val options = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataShowBinding.inflate(inflater, container, false)
        val dateselect = sharedPrefData.callDataString("dateSelect")

        settingLineChart(binding.graphCurrentOut)
        settingLineChart(binding.graphCurrentIn)
        settingLineChart(binding.graphVolt)
        settingLineChart(binding.graphSoC)

        binding.horizontalScroll.isHorizontalScrollBarEnabled = false
        binding.verticalScroll.isVerticalScrollBarEnabled = false
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
        val period = sharedPrefData.callDataString("period")
        val dateSelect = sharedPrefData.callDataString("dateSelect")
        val dateRef = sharedPrefData.callDataString("dateRef")

        fireDatabase.getReference("panels").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    options.clear()
                    for (data in dataSnapshot.children) {
                        val option = data.key.toString()
                        option.let {
                            options.add(option)
                        }
                    }

                    val adapter = ArrayAdapter(requireContext(),
                        com.google.android.material.R.layout.support_simple_spinner_dropdown_item, options)
                    adapter.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item)
                    binding.spinnerDateSelect.adapter = adapter
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
        )

        binding.spinnerDateSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                dataViewModel.dataShow(period,"panels/${options[position]}/$dateRef")
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }

        dataViewModel.data10MinList.observe(viewLifecycleOwner){ data ->
            var count = 0f
            mCurrentOut.clear()
            mCurrentIn.clear()
            mTegangan.clear()
            mTime.clear()
            mSoC.clear()

            data.forEach {
                val entryCurrentIn = Entry(count, it.arusMasuk)
                val entryCurrentOut = Entry(count, it.arusKeluar)
                val entryTegangan = Entry(count, it.tegangan)
                val entrySoC = Entry(count, it.soc)

                mTime.add(it.currentTime)
                mCurrentIn.add(entryCurrentIn)
                mCurrentOut.add(entryCurrentOut)
                mTegangan.add(entryTegangan)
                mSoC.add(entrySoC)

                count += 1f
            }

            dataViewModel.mListDate.observe(viewLifecycleOwner) { list ->
                showListData(data, list, requireContext(), requireActivity().application)
            }
            dataViewModel.mTime.postValue(mTime)
            dataViewModel.mCurrentOut.postValue(mCurrentOut)
            dataViewModel.mCurrentIn.postValue(mCurrentIn)
            dataViewModel.mTegangan.postValue(mTegangan)
            dataViewModel.mSoC.postValue(mSoC)
        }

        dataViewModel.time.observe(viewLifecycleOwner) { time ->
            dataViewModel.currentOut.observe(viewLifecycleOwner) { currentOut ->
                updateLineChart(currentOut, binding.graphCurrentOut, dateSelect)
            }
            dataViewModel.currentIn.observe(viewLifecycleOwner) { currentIn ->
                updateLineChart(currentIn, binding.graphCurrentIn, dateSelect)
            }
            dataViewModel.tegangan.observe(viewLifecycleOwner) { tegangan ->
                updateLineChart(tegangan, binding.graphVolt, dateSelect)
            }
            dataViewModel.stateCharge.observe(viewLifecycleOwner) { soc ->
                updateLineChart(soc, binding.graphSoC, dateSelect)
            }
        }
    }

    private fun showListData(listDate: MutableList<Data10Min>, dateList: List<String>, context: Context, application: Application) {
        val adapter = DataAdapter(listDate, dateList, application)
        binding.rvDataShow.adapter = adapter
        binding.rvDataShow.layoutManager = LinearLayoutManager(context)
    }
}