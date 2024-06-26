package com.elektro.monitoring.ui.data

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

        binding.btnDownload.setOnClickListener {
            exportDataToExcel()
        }

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

            if (period != "Daily"){
                dataViewModel.mListDate.observe(viewLifecycleOwner) { list ->
                    showListData(data, list, requireContext(), requireActivity().application)
                }
            } else {
                showListData(data, null, requireContext(), requireActivity().application)
            }
            dataViewModel.mTime.postValue(mTime)
            dataViewModel.mCurrentOut.postValue(mCurrentOut)
            dataViewModel.mCurrentIn.postValue(mCurrentIn)
            dataViewModel.mTegangan.postValue(mTegangan)
            dataViewModel.mSoC.postValue(mSoC)
        }

        dataViewModel.time.observe(viewLifecycleOwner) {
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

    private fun exportDataToExcel() {
        val period = sharedPrefData.callDataString("period")
        dataViewModel.data10MinList.observe(viewLifecycleOwner) { list ->
            if (period != "Daily") {
                dataViewModel.mListDate.observe(viewLifecycleOwner) { listDate ->
                    createXlsx(requireContext(), list, listDate)
                }
            } else {
                createXlsx(requireContext(), list, null)
            }
        }
    }

    private fun createXlsx(context: Context, listData10Min: List<Data10Min>, dateList: List<String>?) {
        try {
            val dateSelect = sharedPrefData.callDataString("dateSelect")
            val period = sharedPrefData.callDataString("period")

            val root = File(
                Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "FileExcel"
            )
            if (!root.exists()) root.mkdirs()
            val path = File(root, "/$dateSelect.xlsx")

            val workbook = XSSFWorkbook()
            val outputStream = FileOutputStream(path)
            val sheet = workbook.createSheet("Data $dateSelect")

            val headerStyle = workbook.createCellStyle()
            headerStyle.setAlignment(HorizontalAlignment.CENTER)
            headerStyle.fillForegroundColor = IndexedColors.BLUE_GREY.getIndex()
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
            headerStyle.setBorderTop(BorderStyle.MEDIUM)
            headerStyle.setBorderBottom(BorderStyle.MEDIUM)
            headerStyle.setBorderRight(BorderStyle.MEDIUM)
            headerStyle.setBorderLeft(BorderStyle.MEDIUM)
            headerStyle.setFont(workbook.createFont().apply {
                fontHeightInPoints = 12
                fontName = "Times New Roman"
                color = IndexedColors.WHITE.index
                bold = true
            })

            val dataStyle = workbook.createCellStyle()
            dataStyle.setAlignment(HorizontalAlignment.CENTER)
            dataStyle.setFont(workbook.createFont().apply {
                fontHeightInPoints = 12
            })


            val headerTitlesDaily = arrayOf("Time", "Current Charging(A)", "Current Discharging(A)", "Volt(v)", "SoC(%)")
            val headerTitles = arrayOf("Date") + headerTitlesDaily

            val headers = if (period != "Daily") headerTitles else headerTitlesDaily
            val headerRow = sheet.createRow(0)

            headers.forEachIndexed { index, title ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(title)
                cell.cellStyle = headerStyle
            }

            listData10Min.forEachIndexed { index, data ->
                val row = sheet.createRow(index + 1)

                if (period != "Daily") {
                    row.createCell(0).apply {
                        setCellValue(dateList?.get(index) ?: "")
                        cellStyle = dataStyle
                    }
                }
                row.createCell(if (period != "Daily") 1 else 0).apply {
                    setCellValue(data.currentTime)
                    cellStyle = dataStyle
                }
                row.createCell(if (period != "Daily") 2 else 1).apply {
                    setCellValue(data.arusMasuk.toString())
                    cellStyle = dataStyle
                }
                row.createCell(if (period != "Daily") 3 else 2).apply {
                    setCellValue(data.arusKeluar.toString())
                    cellStyle = dataStyle
                }
                row.createCell(if (period != "Daily") 4 else 3).apply {
                    setCellValue(data.tegangan.toString())
                    cellStyle = dataStyle
                }
                row.createCell(if (period != "Daily") 5 else 4).apply {
                    setCellValue(data.soc.toString())
                    cellStyle = dataStyle
                }
            }

            headers.indices.forEach {
                sheet.setColumnWidth(it, 104 * 64)
            }

            workbook.write(outputStream)
            outputStream.close()
            workbook.close()

            Toast.makeText(context, "Data berhasil diekspor!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal menyimpan file!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showListData(listDate: MutableList<Data10Min>, dateList: List<String>?, context: Context, application: Application) {
        val adapter = DataAdapter(listDate, dateList, application)
        binding.rvDataShow.adapter = adapter
        binding.rvDataShow.layoutManager = LinearLayoutManager(context)
    }
}