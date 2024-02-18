package com.elektro.monitoring.helper.utils

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

fun updateLineChart(
    entries: MutableList<Entry>,
    lineChart: LineChart,
    tanggal: String,
    mTime: MutableList<String>
) {
    val lineDataSet = LineDataSet(entries, tanggal)
    val lineData = LineData(lineDataSet)
    val description = Description()

    lineChart.data = lineData
    description.text = ""
    lineChart.description = description
    val leftYAxis = lineChart.axisLeft
    val rightYAxis = lineChart.axisRight
    rightYAxis.isEnabled = false
    leftYAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
    lineChart.notifyDataSetChanged()
    lineChart.invalidate()
}

fun settingLineChart(lineChart: LineChart) {
    lineChart.setPinchZoom(false)
    lineChart.setDragEnabled(false)
    lineChart.setTouchEnabled(false)
}