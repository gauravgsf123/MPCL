package com.mpcl.activity.operation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.mpcl.R
import com.mpcl.adapter.VechileLoadAdapter
import com.mpcl.database.ObjectBox
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.database.*
import com.mpcl.databinding.ActivityVehicleLoadUploadBinding
import com.mpcl.databinding.DialogStatusViewListBinding
import com.mpcl.model.VehicleLoadRequest
import com.mpcl.model.VehicleResponseModel
import com.mpcl.util.qr_scanner.QRcodeScanningActivity
import com.mpcl.viewmodel.VehicleLoanUnloadViewModel.VechileLoanUnloadRepository
import com.mpcl.viewmodel.VehicleLoanUnloadViewModel.VechileLoanUnloadViewModel
import com.mpcl.viewmodel.VehicleLoanUnloadViewModel.VechileLoanUnloadViewModelFactory
import io.objectbox.Box

import com.mpcl.adapter.StatusViewListAdapter
import com.mpcl.model.ScanDocTotalResponseModel
import java.util.*
import kotlin.collections.ArrayList


class VehicleLoadUploadActivity : BaseActivity(),TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityVehicleLoadUploadBinding
    private var totalLeftBox :Int?=0
    private val REQUEST_CAMERA_CAPTURE = 100
    private lateinit var managePermissions: ManagePermissions
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private val permissionList = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var vechileLoanUnloadRepository: VechileLoanUnloadRepository
    private lateinit var vechileLoanUnloadViewModel: VechileLoanUnloadViewModel
    private lateinit var vechileLoanUnloadViewModelFactory: VechileLoanUnloadViewModelFactory

    private var valueList: MutableList<String> = mutableListOf()
    private var nameList: MutableList<String> = mutableListOf()
    private var preFixList: MutableList<String> = mutableListOf()
    private lateinit var docType: String
    private var enableTrue: Boolean = false
    private var callOneTime: Boolean = false
    private var vechileDataBox: Box<VechileData>? = null
    private var vechileListDataBox: Box<VehicleListData>? = null
    private var isCamera = false
    lateinit var dialog: AlertDialog
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleLoadUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vechileDataBox = ObjectBox.boxStore.boxFor(VechileData::class.java)
        vechileListDataBox = ObjectBox.boxStore.boxFor(VehicleListData::class.java)
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        vechileLoanUnloadRepository = VechileLoanUnloadRepository()
        vechileLoanUnloadViewModelFactory =
            VechileLoanUnloadViewModelFactory(vechileLoanUnloadRepository)
        vechileLoanUnloadViewModel = ViewModelProvider(this, vechileLoanUnloadViewModelFactory).get(
            VechileLoanUnloadViewModel::class.java
        )
        binding.stockListRecyclerview.adapter = VechileLoadAdapter().apply {
            itemClick = { scan ->

            }
        }
        tts = TextToSpeech(this, this)

        getDoctype()
        setObserver()
        enableTrue = sharedPreference.getValueBoolean(Constant.VECHICLE_ENABLE, false)
        if (enableTrue) {
            binding.type.isEnabled = false
            //binding.documentNo.isEnabled = false
            enableDocument(false)
            binding.check.visibility = View.GONE
            binding.boxNo.visibility = View.VISIBLE
            binding.ivPrint.visibility = View.VISIBLE
            binding.type.setText(sharedPreference.getValueString(Constant.VECHICLE_POSITION)!!)
            docType = sharedPreference.getValueString(Constant.DOCTYPE)!!
            //binding.documentNo4.setText(sharedPreference.getValueString(Constant.DOCUMENT)!!)
            setDocumentNo()
            vechileListDataBox?.let { it1 -> setRecylatView(it1?.all)}
            binding.totalScan.text = "${sharedPreference.getValueInt(Constant.TOTAL_DOC)}"
        } else {
            binding.type.isEnabled = true
            enableDocument(true)
            binding.check.visibility = View.VISIBLE
            binding.boxNo.visibility = View.GONE
            binding.ivPrint.visibility = View.GONE
            vechileListDataBox?.let { it1 -> setRecylatView(it1?.all)}
        }


        binding.submit.setOnClickListener {
            showDialog(false)
            getJSon(vechileDataBox?.all!!)
            //vechileLoanUnloadViewModel.uploadVehicleScan(finalJSON)
        }


        binding.type.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selected = parent.getItemAtPosition(position)
                docType = valueList[position]
                sharedPreference.save(Constant.VECHICLE_POSITION, nameList[position])
                sharedPreference.save(Constant.DOCTYPE, docType)
                when (docType!!) {
                    /*"PRS" -> {
                        showHide(true);binding.documentNo3.setText("PR")
                    }*/
                    "MFIN" -> {
                        showHide(false)
                    }
                    /*"MFOUT" -> {
                        showHide(true);binding.documentNo3.setText("MF")
                    }
                    "DRS", "DRS" -> {
                        showHide(true);binding.documentNo3.setText("DR")
                    }*/
                    else ->{
                        showHide(true); binding.documentNo3.setText(preFixList[position])
                    }
                }

            }

        binding.boxNo.doOnTextChanged { text, start, count, after ->
            if (!TextUtils.isEmpty(binding.boxNo.text.toString().trim())) {
                findDocumentNo(binding.boxNo.text.toString())
            }
        }

        binding.ivPrint.setOnClickListener {
            isCamera = true
            managePermissions.checkPermissions()
            selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
            startScanning()
        }

        binding.boxNo.setOnClickListener { isCamera = false }

        binding.view.setOnClickListener{
            callViewDocumentListAPI()
        }


        binding.check.setOnClickListener {
            val mScanDocDataBody = mapOf<String, String>(
                "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                "BID" to sharedPreference.getValueString(Constant.BID)!!,
                "DOCNUMBER" to getDocumentNo(),
                "DOCTYPE" to docType
            )
            showDialog()
            callOneTime = false
            Log.d(TAG, mScanDocDataBody.toString())
            vechileLoanUnloadViewModel.getVehicleDataList(mScanDocDataBody)
        }

        binding.reset.setOnClickListener {
            vechileDataBox?.removeAll()
            binding.type.isEnabled = true
            enableDocument(true)
            enableTrue = false
            binding.type.setText(resources.getString(R.string.select_option))

            binding.check.visibility = View.VISIBLE
            binding.submit.visibility = View.GONE
            binding.boxNo.visibility = View.GONE
            binding.ivPrint.visibility = View.GONE
            sharedPreference.save(Constant.VECHICLE_ENABLE, enableTrue)
            vechileListDataBox?.removeAll()
            vechileListDataBox?.all?.let { setRecylatView(it) }
            getDoctype()
        }
    }

    private fun setObserver(){

        vechileLoanUnloadViewModel.docTypeListResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                //responseModel[0].branch?.let { it1 -> Log.d(TAG, it1) }
                valueList.clear()
                nameList.clear()
                preFixList.clear()
                for (branch in responseModel) {
                    valueList.add(branch.Value!!)
                    nameList.add(branch.Name!!)
                    preFixList.add(branch.Prefix!!)
                }
                val traderTypeAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, nameList)
                binding.type.setAdapter(traderTypeAdapter)

            } else {
                showError(
                    getString(R.string.opps),
                    "Some thing wrong"
                )
            }
        })
        /*vechileLoanUnloadViewModel.scanDocDataResponse.observe(this, Observer {
            hideDialog()
            Log.d(TAG, it.toString())
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                if (responseModel[0].Message.isNullOrBlank()) {
                    if (!callOneTime) {
                        callScanTotalAPI()
                        callOneTime = true
                    }
                } else {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(responseModel[0].Response)
                        .setContentText(responseModel[0].Message)
                        .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { dialog ->
                            dialog.dismiss()
                        })
                        .show()
                }


            } else {
                showError(
                    getString(R.string.opps),
                    "Some thing wrong"
                )
            }
        })*/

        vechileLoanUnloadViewModel.scanDocTotalResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                Log.d(TAG, responseModel.toString())
                showList(responseModel)

            } else {
                showError(
                    getString(R.string.opps),
                    "No Data Found"
                )
            }
        })

        vechileLoanUnloadViewModel.uploadVehicleScanResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            Log.d(TAG, responseModel.toString())
            vechileDataBox?.removeAll()
            vechileListDataBox?.removeAll()
            //setRecylatView()
            binding.type.isEnabled = false
            //binding.documentNo.isEnabled = false
            enableDocument(false)
            enableTrue = false
            binding.check.visibility = View.VISIBLE
            binding.submit.visibility = View.GONE
            binding.boxNo.visibility = View.VISIBLE
            binding.ivPrint.visibility = View.VISIBLE
            sharedPreference.save(Constant.VECHICLE_ENABLE, enableTrue)
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Data successfully uploaded on server")
                .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { sweetAlert ->
                    sweetAlert.dismiss()
                    val mScanDocDataBody = mapOf<String, String>(
                        "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                        "BID" to sharedPreference.getValueString(Constant.BID)!!,
                        "DOCNUMBER" to getDocumentNo(),
                        "DOCTYPE" to docType
                    )
                    showDialog()
                    callOneTime = false
                    Log.d(TAG, mScanDocDataBody.toString())
                    vechileLoanUnloadViewModel.getVehicleDataList(mScanDocDataBody)
                })
                .show()
        })

        vechileLoanUnloadViewModel.vehicleResponseModel.observe(this, Observer { it ->
            hideDialog()
            val responseModel = it ?: return@Observer
            Log.d(TAG, responseModel.toString())
            if (responseModel.isNotEmpty()) {
                binding.type.isEnabled = false
                enableDocument(false)
                nameList.clear()
                vechileListDataBox?.removeAll()
                vechileDataBox?.removeAll()
                val traderTypeAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, nameList)
                binding.type.setAdapter(traderTypeAdapter)
                enableTrue = true
                binding.check.visibility = View.GONE
                binding.boxNo.visibility = View.VISIBLE
                binding.ivPrint.visibility = View.VISIBLE

                sharedPreference.save(Constant.VECHICLE_ENABLE, enableTrue)
                sharedPreference.save(Constant.DOCUMENT, getDocumentNo())
                sharedPreference.save(Constant.TOTAL_DOC, responseModel.size)
                binding.tvScanStatus.text = ""
                responseModel.forEach {vehicle->
                    vechileListDataBox?.put(VehicleListData(0,vehicle.Response,vehicle.Destination,vehicle.CNoteNo,vehicle.BarCodeNo,vehicle.ChgWeight))
                }
                vechileListDataBox?.let {
                        it1 -> setRecylatView(it1?.all)
                    binding.totalScan.text = "${it1.all.size}"
                }


            } else {
                showError(
                    getString(R.string.opps),
                    getString(R.string.data_not_available)
                )
            }
        })

        vechileLoanUnloadViewModel.sendExtraScanResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            Log.d("sendExtraScanResponse",responseModel.toString())
        })
    }

    private fun showList(responseModel: List<ScanDocTotalResponseModel>) {

        val alertDialog = AlertDialog.Builder(this@VehicleLoadUploadActivity)
        val inflater = layoutInflater
        val convertView: View = inflater.inflate(R.layout.dialog_status_view_list, null)
        alertDialog.setView(convertView)
        var binding = DialogStatusViewListBinding.bind(convertView)
        binding.rvStatusViewList.adapter = StatusViewListAdapter()
        (binding.rvStatusViewList.adapter as StatusViewListAdapter).setItems(
            responseModel,this
        )


        alertDialog.show()
        alertDialog.setCancelable(true)
    }

    private fun getDoctype(){
        val body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!
        )
        vechileLoanUnloadViewModel.getDocTypeList(body)
    }

    private fun showHide(isShow: Boolean) {
        if (isShow) {
            binding.documentNo1.visibility = View.VISIBLE
            binding.documentNo2.visibility = View.VISIBLE
            binding.documentNo3.visibility = View.VISIBLE
        } else {
            binding.documentNo1.visibility = View.GONE
            binding.documentNo2.visibility = View.GONE
            binding.documentNo3.visibility = View.GONE
        }
    }

    fun getJSon(scanCode: List<VechileData>) {

        var dataRequest = ArrayList<VehicleLoadRequest.Data>()
        for (value in scanCode) {
            dataRequest.add(VehicleLoadRequest.Data(value.bar_code?.trim()!!))
        }

        var vecicleloadRequst = VehicleLoadRequest(sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            sharedPreference.getValueString(Constant.BID)!!,
            sharedPreference.getValueString(Constant.DOCUMENT)!!,
            sharedPreference.getValueString(Constant.DOCTYPE)!!,
            sharedPreference.getValueString(Constant.EMP_NO)!!,
            dataRequest!!)
        var jsonData = Gson().toJson(vecicleloadRequst)
        vechileLoanUnloadViewModel.uploadNewVehicleScan(vecicleloadRequst)
    }


    override fun onPostResume() {
        super.onPostResume()
        if (sharedPreference.getValueString("result")!!.isNotEmpty()) {
            var str = sharedPreference.getValueString("result")
            Log.d(TAG, str!!)
            binding.boxNo.setText(str)
            sharedPreference.removeValue("result")

        }
    }

    private fun findDocumentNo(barcode: String) {
            var stockData = vechileDataBox?.query(VechileData_.bar_code.equal(barcode))?.build()?.findFirst()
            if (stockData != null && stockData.isMatch==true) {
                binding.tvScanStatus.setTextColor(resources.getColor(R.color.yellow))
                binding.tvScanStatus.text = getString(R.string.barcode_already_scan)
                checkListSize()
            } else {
                var barCodeData = vechileListDataBox?.query(VehicleListData_.BarCodeNo.equal(barcode))?.build()?.findFirst()
                if(barCodeData!=null && !barCodeData.Destination.isNullOrEmpty()){
                    vechileDataBox?.put(barcode?.let { it1 -> getData(it1,true) })
                    barCodeData?.isScan = true
                    vechileListDataBox?.put(barCodeData)
                    binding.tvScanStatus.setTextColor(resources.getColor(R.color.green))
                    binding.tvScanStatus.text = "${getString(R.string.success)} ChgWet: ${barCodeData.ChgWeight}"
                    speakOut(barCodeData.Destination!!)
                    checkListSize()
                }else{
                    var barCodeData = vechileListDataBox?.query(VehicleListData_.BarCodeNo.equal(barcode))?.build()?.findFirst()
                    if(barCodeData!=null){
                        binding.tvScanStatus.setTextColor(resources.getColor(R.color.red))
                        binding.tvScanStatus.text = getString(R.string.wrong_barcode_scan)
                        sendExtraScan(barcode)
                        checkListSize()
                    }else{
                        vechileDataBox?.put(barcode?.let { it1 -> getData(it1,false) })
                        var barCodeData = VehicleListData(0,"","","",barcode,"",true)
                        vechileListDataBox?.put(barCodeData)
                        binding.tvScanStatus.setTextColor(resources.getColor(R.color.red))
                        binding.tvScanStatus.text = getString(R.string.wrong_barcode_scan)
                        sendExtraScan(barcode)
                        checkListSize()
                    }

                }
                vechileListDataBox?.all?.let { setRecylatView(it) }


            }

        /*} else {
            var str = sharedPreference.getValueString("result")
            //str?.let { scanList.add(it) }
            vechileDataBox?.put(str?.let { it1 -> getData(it1) })
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Barcode")
                .setContentText(str)
                .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener {

                    it.dismiss()
                    if (isCamera) startScanning()
                })
                .show()
        }*/
        sharedPreference.removeValue("result")
        binding.boxNo.setText("")
    }

    private fun sendExtraScan(barcode: String){
        val mScanDocDataBody = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "EMPNO" to sharedPreference.getValueString(Constant.EMP_NO)!!,
            "DOCNUMBER" to sharedPreference.getValueString(Constant.DOCUMENT)!!,
            "DOCTYPE" to docType,
            "BARCODENO" to barcode,

        )
        vechileLoanUnloadViewModel.sendExtraScan(mScanDocDataBody)
    }

    private fun checkListSize(){
        if(vechileDataBox?.all?.size!!>=50){
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Upload your data")
                .setContentText("Please submit scan data and rescan again")
                .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { sweetAlert ->
                    sweetAlert.dismiss()
                    showDialog(false)
                    getJSon(vechileDataBox?.all!!)
                })
                .show()

        }else{
            if (isCamera) startScanning()
        }
    }


    private fun setRecylatView(data: MutableList<VehicleListData>) {
        var count = 0
        vechileDataBox?.all?.forEach{
            if(it.isMatch == true)  count++
        }
        binding.scan.text = "$count"
        data.sortBy { it.isScan==true }
        if (data.size!! > 0) {
            Log.d(TAG, data.size.toString())
            binding.constraintLayout.visibility = View.VISIBLE
            binding.submit.visibility = View.VISIBLE
            (binding.stockListRecyclerview.adapter as VechileLoadAdapter).setItems(
                data as List<VehicleListData>,this
            )
        } else {
            binding.constraintLayout.visibility = View.GONE
            binding.submit.visibility = View.GONE
        }
    }


    private fun getData(item: String,is_match:Boolean): VechileData {
        val stockData = VechileData()
        stockData.bar_code = item
        stockData.isMatch = is_match

        return stockData
    }

    private fun getData(item: VehicleResponseModel): VehicleListData {
        val stockData = VehicleListData()
        stockData.Id = 0
        stockData.Response = item.Response
        stockData.BarCodeNo = item.BarCodeNo
        stockData.CNoteNo = item.CNoteNo
        stockData.Destination = item.Response


        return stockData
    }

    private fun callViewDocumentListAPI() {
        val mScanDocDataBody = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "DOCNUMBER" to sharedPreference.getValueString(Constant.DOCUMENT)!!,
            "DOCTYPE" to docType
        )
        showDialog()
        vechileLoanUnloadViewModel.scanDocTotal(mScanDocDataBody)
    }

    private fun openCameraWithScanner() {
        QRcodeScanningActivity.start(this, selectedScanningSDK)
    }

    private fun startScanning() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCameraWithScanner()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_CAPTURE
            )
        }
    }

    override fun onStop() {
        super.onStop()
        //sharedPreference.save(Constant.VECHICLE_ENABLE,false)
    }

    fun enableDocument(enable: Boolean) {
        binding.documentNo1.isEnabled = enable
        binding.documentNo2.isEnabled = enable
        binding.documentNo3.isEnabled = enable
        binding.documentNo4.isEnabled = enable
        if (enable) {
            binding.documentNo1.setText("")
            binding.documentNo2.setText("")
            binding.documentNo3.setText("")
            binding.documentNo4.setText("")
        }
    }

    fun getDocumentNo(): String {
        var documentno = ""
        when (docType) {
            "PRS" ->{
                documentno =
                    "${binding.documentNo1.text.toString()}/${binding.documentNo2.text.toString()}/${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
            "MFIN" -> {
                documentno = "${binding.documentNo4.text.toString()}"
            }
            "MFOUT" -> {
                documentno =
                    "${binding.documentNo1.text.toString()}/${binding.documentNo2.text.toString()}/${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
            "DRS", "DRS" ->{
                documentno =
                    "${binding.documentNo1.text.toString()}/${binding.documentNo2.text.toString()}/${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
            else -> {
                documentno =
                    "${binding.documentNo1.text.toString()}/${binding.documentNo2.text.toString()}/${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
        }
        return documentno
    }

    fun setDocumentNo() {
        docType = sharedPreference.getValueString(Constant.DOCTYPE)!!
        var documentNo = sharedPreference.getValueString(Constant.DOCUMENT)!!
        var docNo = documentNo.split("/")
        when (docType) {
            "PRS" -> {
                binding.documentNo1.setText(docNo[0])
                binding.documentNo2.setText(docNo[1])
                binding.documentNo3.setText(docNo[2].substring(0,2))
                binding.documentNo4.setText(docNo[2].substring(2))
            }

            "MFIN" -> {
                binding.documentNo4.setText(sharedPreference.getValueString(Constant.DOCUMENT)!!)
            }
            "MFOUT" -> {
                binding.documentNo1.setText(docNo[0])
                binding.documentNo2.setText(docNo[1])
                binding.documentNo3.setText(docNo[2].substring(0,2))
                binding.documentNo4.setText(docNo[2].substring(2))
            }
            "DRS", "DRS" -> {
                binding.documentNo1.setText(docNo[0])
                binding.documentNo2.setText(docNo[1])
                binding.documentNo3.setText(docNo[2].substring(0,2))
                binding.documentNo4.setText(docNo[2].substring(2))
            }
            else -> {
                binding.documentNo1.setText(docNo[0])
                binding.documentNo2.setText(docNo[1])
                binding.documentNo3.setText(docNo[2].substring(0,2))
                binding.documentNo4.setText(docNo[2].substring(2))
            }
        }
    }

    private fun speakOut(text: String) {
        //val text = text
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts tts.setLanguage(new Locale("hi"));
            val result = tts!!.setLanguage(Locale("hi", "IN"))
            tts!!.setPitch(1.0f) // saw from internet
            tts!!.setSpeechRate(0.8f)

            val voices = tts!!.voices
            for (voice in voices) {
                Log.v(TAG, voice.name)
                if (voice.name == "hi-in-x-cfn#female_2-local") {
                    tts!!.voice = voice
                }
            }
            //tts!!.voice = Voice(Raw)// f denotes float, it actually type casts 0.5 to float
            //tts!!.setLanguage(Locale.US);


            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                //buttonSpeak!!.isEnabled = true
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }
}