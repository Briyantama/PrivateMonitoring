package com.elektro.monitoring.ui.data

import android.R
import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.elektro.monitoring.helper.Constants.TAG
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.model.Data10Min
import com.elektro.monitoring.ui.adapter.DataAdapter
import com.elektro.monitoring.viewmodel.DataViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
        val dateselect = sharedPrefData.callDataString("dateSelect")
        val ref = sharedPrefData.callDataString("dataRef")

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
                        R.layout.simple_spinner_item, options)
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
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
                fireDatabase.getReference("panels/${options[position]}/$ref").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        dataViewModel.mdata10MinList.value?.clear()
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
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }

        dataViewModel.data10MinList.observe(viewLifecycleOwner){ data ->
            showListData(data, requireContext())
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

            dataViewModel.mTime.postValue(mTime)
            dataViewModel.mCurrentOut.postValue(mCurrentOut)
            dataViewModel.mCurrentIn.postValue(mCurrentIn)
            dataViewModel.mTegangan.postValue(mTegangan)
            dataViewModel.mSoC.postValue(mSoC)
        }

        dataViewModel.time.observe(viewLifecycleOwner) { time ->
            dataViewModel.currentOut.observe(viewLifecycleOwner) { currentOut ->
                updateLineChart(currentOut, binding.graphCurrentOut, dateselect, time, "Arus Keluar")
            }
            dataViewModel.currentIn.observe(viewLifecycleOwner) { currentIn ->
                updateLineChart(currentIn, binding.graphCurrentIn, dateselect, time, "Arus Masuk")
            }
            dataViewModel.tegangan.observe(viewLifecycleOwner) { tegangan ->
                updateLineChart(tegangan, binding.graphVolt, dateselect, time, "Tegangan")
            }
            dataViewModel.stateCharge.observe(viewLifecycleOwner) { soc ->
                updateLineChart(soc, binding.graphSoC, dateselect, time, "State of Charge")
            }
        }
    }

    private fun showListData(listDate: MutableList<Data10Min>, context: Context) {
        val adapter = DataAdapter(listDate)
        binding.rvDataShow.adapter = adapter
        binding.rvDataShow.layoutManager = LinearLayoutManager(context)
    }

    private fun updateLineChart(
        entries: MutableList<Entry>,
        lineChart: LineChart,
        tanggal: String,
        mTime: MutableList<String>,
        mDescription: String
    ) {
        val lineDataSet = LineDataSet(entries, tanggal)
        val lineData = LineData(lineDataSet)
        val description = Description()

        lineChart.data = lineData
        description.text = mDescription
        lineChart.description = description
        val leftYAxis = lineChart.axisLeft
        val rightYAxis = lineChart.axisRight
        rightYAxis.isEnabled = false
        leftYAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(mTime)
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    private fun settingLineChart(lineChart: LineChart) {
        lineChart.setTouchEnabled(false)
        lineChart.setPinchZoom(false)
        lineChart.isDragEnabled = false
    }
}