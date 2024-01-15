package com.elektro.monitoring.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.model.Data10Min
import com.elektro.monitoring.model.DataNow
import com.elektro.monitoring.model.NotifikasiSuhu
import com.github.mikephil.charting.data.Entry
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DataViewModel @Inject constructor(private val sharedPrefData: SharedPrefData) : ViewModel() {

    private val random = Random.Default
    private val sdf = SimpleDateFormat("HH:mm:ss,SSS", Locale.getDefault())
    private val tf = SimpleDateFormat("EEEE, dd-MMMM-yyyy", Locale.getDefault())
    private val decimalFormat = DecimalFormat("#.###")
    private val decimalFormatSuhu = DecimalFormat("#.#")

    private val fireDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    val mDataNow = MutableLiveData<DataNow>()
    val dataNow : LiveData<DataNow> = mDataNow
    val listNotif = MutableLiveData<MutableList<NotifikasiSuhu>>()
    val listDate = MutableLiveData<MutableList<String>>()

    val mCurrentIn = MutableLiveData<MutableList<Entry>>()
    val currentIn: LiveData<MutableList<Entry>> get() = mCurrentIn

    val mTegangan = MutableLiveData<MutableList<Entry>>()
    val tegangan: LiveData<MutableList<Entry>> get() = mTegangan

    val mSoC = MutableLiveData<MutableList<Entry>>()
    val stateCharge: LiveData<MutableList<Entry>> get() = mSoC

    val mCurrentOut = MutableLiveData<MutableList<Entry>>()
    val currentOut: LiveData<MutableList<Entry>> get() = mCurrentOut

    val mTime = MutableLiveData<MutableList<String>>()
    val time: LiveData<MutableList<String>> get() =  mTime

    var mdata10MinList = MutableLiveData<MutableList<Data10Min>>()
    val data10MinList : LiveData<MutableList<Data10Min>> get() = mdata10MinList

    private var stat = false

    fun update() {
        viewModelScope.launch(Dispatchers.Main) {
            val tanggal = tf.format(Calendar.getInstance().time)
            val jam = sdf.format(Calendar.getInstance().time)

            val soc = sharedPrefData.totalSoC()
            val mArusKeluar = sharedPrefData.totalData("totalArusKeluar")
            val mArusMasuk = sharedPrefData.totalData("totalArusMasuk")
            val mTegangan = sharedPrefData.totalData("totalTegangan")
            val count = sharedPrefData.totalData("count")

            val valueArusKeluar = 2.2f + random.nextFloat() * (3.3f - 2.2f)
            val valueArusMasuk = 2.2f + random.nextFloat() * (3.3f - 2.2f)
            val valueTegangan = 10f + random.nextFloat() * (13.8f - 10f)
            val valueSuhu = 20f + random.nextFloat() * (40f - 20f)

            sharedPrefData.sumData("count", count, 1f)
            sharedPrefData.sumData("totalArusKeluar", mArusKeluar, valueArusKeluar)
            sharedPrefData.sumData("totalArusMasuk", mArusMasuk, valueArusMasuk)
            sharedPrefData.sumData("totalTegangan", mTegangan, valueTegangan)

            if (count >= 600f) {
                sendData10Min(
                    soc,
                    mArusKeluar,
                    mArusMasuk,
                    jam,
                    mTegangan,
                    tanggal
                )
            }

            postToFirebase1Sec(
                soc,
                valueArusKeluar,
                valueArusMasuk,
                jam,
                valueSuhu,
                valueTegangan,
            )
        }
    }

    private fun mean(total: Float): Float {
        return total/600f
    }

    private fun sendData10Min(
        soc: Float,
        arusKeluar: Float,
        arusMasuk: Float,
        currentTime: String,
        tegangan: Float,
        date: String
    ) {
        var msoc = soc
        val meanArusKeluar = mean(arusKeluar)
        val meanArusMasuk = mean(arusMasuk)
        val meanTegangan = mean(tegangan)

        if (soc==100f) {
            stat = false
        }else if (soc==20f){
            stat = true
        }

        if (!stat){
            sharedPrefData.decreaseSoC(soc)
            msoc-=1f
        }else {
            sharedPrefData.increaseSoC(soc)
            msoc+=1f
        }

        sharedPrefData.clearFloat()

        postToFirebase10Min(
            msoc,
            meanArusKeluar,
            meanArusMasuk,
            currentTime,
            meanTegangan,
            date
        )
    }

    private fun postToFirebase10Min(
        soc: Float,
        arusKeluar: Float,
        arusMasuk: Float,
        currentTime: String,
        tegangan: Float,
        date: String
    ) {
        val sizeData = sharedPrefData.callDataInt("sizeData") + 1
        var sizeDate = sharedPrefData.callDataInt("sizeDate")
        val datePanel = sharedPrefData.callDataString("date")
        val data10Min = Data10Min(
            decimalFormatSuhu.format(soc).toFloat(),
            decimalFormat.format(arusKeluar).toFloat(),
            decimalFormat.format(arusMasuk).toFloat(),
            currentTime,
            decimalFormat.format(tegangan).toFloat()
        )

        if (date!=datePanel){
            sizeDate+=1
        }

        fireDatabase.getReference("panels/Solar A/$sizeDate/$date/$sizeData")
            .setValue(data10Min)
    }

    private fun postToFirebase1Sec(
        soc: Float,
        arusKeluar: Float,
        arusMasuk: Float,
        currentTime: String,
        suhu: Float,
        tegangan: Float
    ) {
        val data = DataNow(
            decimalFormatSuhu.format(soc).toFloat(),
            decimalFormat.format(arusKeluar).toFloat(),
            decimalFormat.format(arusMasuk).toFloat(),
            currentTime,
            decimalFormatSuhu.format(suhu).toFloat(),
            decimalFormat.format(tegangan).toFloat()
        )
        fireDatabase.getReference("dataNow").setValue(data)
    }
}