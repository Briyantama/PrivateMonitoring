package com.elektro.monitoring.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.elektro.monitoring.R
import com.elektro.monitoring.databinding.FragmentHomeBinding
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.helper.utils.getMonth
import com.elektro.monitoring.helper.utils.getTanggal
import com.elektro.monitoring.helper.utils.getWeekNumber
import com.elektro.monitoring.helper.utils.invertBulan
import com.elektro.monitoring.helper.utils.settingLineChart
import com.elektro.monitoring.helper.utils.updateLineChart
import com.elektro.monitoring.model.Data10Min
import com.elektro.monitoring.model.DataNow
import com.elektro.monitoring.viewmodel.DataViewModel
import com.github.mikephil.charting.data.Entry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var sharedPrefData: SharedPrefData
    private val binding get() = _binding!!
    private var _binding: FragmentHomeBinding? = null

    private val dataViewModel: DataViewModel by viewModels()
    private val mCurrentIn: MutableList<Entry> = mutableListOf()

    private val mCurrentOut: MutableList<Entry> = mutableListOf()
    private val mTime: MutableList<String> = mutableListOf()
    private val mTegangan: MutableList<Entry> = mutableListOf()
    private val mSoC: MutableList<Entry> = mutableListOf()

    private val fireDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val options = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        settingLineChart(binding.graphCurrentOut)
        settingLineChart(binding.graphCurrentIn)
        settingLineChart(binding.graphVolt)
        settingLineChart(binding.graphSoC)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivNotif.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_homeFragment_to_notificationFragment)
        }

        binding.ivHistory.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_homeFragment_to_dateSelectFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        val today = sharedPrefData.callDataString("today")

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
                        android.R.layout.simple_spinner_item, options)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerHome.adapter = adapter
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            }
        )

        binding.spinnerHome.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                sharedPrefData.editDataString("selectedpanel", options[position])
                val date = getTanggal(today)
                val bulan = invertBulan(getMonth(today))
                val weekInt = getWeekNumber(today)

                fireDatabase.getReference("panels/${options[position]}/$bulan/$weekInt/$date/$today")
                    .limitToLast(7).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            dataViewModel.mdata10MinList.value?.clear()
                            val listData: MutableList<Data10Min> = mutableListOf()

                            snapshot.children.forEach { value ->
                                val newData = value.getValue(Data10Min::class.java)
                                newData?.let { listData.add(it) }
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

        fireDatabase.getReference("dataNow").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val new = snapshot.getValue(DataNow::class.java)
                dataViewModel.mDataNow.postValue(new)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        dataViewModel.data10MinList.observe(viewLifecycleOwner) { data ->
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
                if (count == 8f) {
                    count = 1f
                }
            }

            dataViewModel.mTime.postValue(mTime)
            dataViewModel.mCurrentOut.postValue(mCurrentOut)
            dataViewModel.mCurrentIn.postValue(mCurrentIn)
            dataViewModel.mTegangan.postValue(mTegangan)
            dataViewModel.mSoC.postValue(mSoC)
        }

        dataViewModel.time.observe(viewLifecycleOwner) { time ->
            dataViewModel.currentOut.observe(viewLifecycleOwner) { currentOut ->
                updateLineChart(currentOut, binding.graphCurrentOut, today)
            }
            dataViewModel.currentIn.observe(viewLifecycleOwner) { currentIn ->
                updateLineChart(currentIn, binding.graphCurrentIn, today)
            }
            dataViewModel.tegangan.observe(viewLifecycleOwner) { tegangan ->
                updateLineChart(tegangan, binding.graphVolt, today)
            }
            dataViewModel.stateCharge.observe(viewLifecycleOwner) { soc ->
                updateLineChart(soc, binding.graphSoC, today)
            }
        }

        dataViewModel.dataNow.observe(viewLifecycleOwner) {
            val arusMasuk = "${it.arusMasuk}A"
            val arusKeluar = "${it.arusKeluar}A"
            val tegangan = "${it.tegangan}v"
            val soc = "${it.soc}%"
            val suhu = "${it.suhu}°C"

            binding.showArusMasuk.text = arusMasuk
            binding.showArusKeluar.text = arusKeluar
            binding.showTegangan.text = tegangan
            binding.showSoc.text = soc
            binding.showSuhu.text = suhu
        }
    }
}