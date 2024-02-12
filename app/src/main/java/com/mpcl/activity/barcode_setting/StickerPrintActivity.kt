package com.mpcl.activity.barcode_setting

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.tscdll.TSCActivity
import com.mpcl.adapter.StickerPrintListAdapter
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.databinding.ActivityStickerPrintBinding
import com.mpcl.model.StickerDataResponseModel
import com.mpcl.util.qr_scanner.QRcodeScanningActivity
import com.mpcl.viewmodel.stickerDataViewModel.StickerDataRepository
import com.mpcl.viewmodel.stickerDataViewModel.StickerDataViewModel
import com.mpcl.viewmodel.stickerDataViewModel.StickerDataViewModelFactory
import java.lang.Exception

class StickerPrintActivity : BaseActivity(),View.OnClickListener {
    var TscDll = TSCActivity()
    private lateinit var binding:ActivityStickerPrintBinding
    private var cNoteList= mutableListOf<StickerDataResponseModel>()
    private var cNote : StickerDataResponseModel?=null
    private lateinit var stickerDataRepository: StickerDataRepository
    private lateinit var stickerDataViewModel: StickerDataViewModel
    private lateinit var stickerDataViewModelFactory: StickerDataViewModelFactory
    private lateinit var managePermissions: ManagePermissions
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private var isCamera = false

    private val REQUEST_CAMERA_CAPTURE = 1002
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStickerPrintBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        stickerDataRepository = StickerDataRepository()
        stickerDataViewModelFactory = StickerDataViewModelFactory(stickerDataRepository)
        stickerDataViewModel = ViewModelProvider(this,stickerDataViewModelFactory).get(
            StickerDataViewModel::class.java)
        setupUI()
        setObserver()

        //binding.barCode.showSoftInputOnFocus = false

        binding.barCode.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            val inputMethod: InputMethodManager =
                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethod != null) {
                inputMethod.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        /*binding.barCode.setOnFocusChangeListener {
                view, b ->
            val inputMethod: InputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if(b) inputMethod.hideSoftInputFromWindow(view.windowToken, 0)
        }*/

        binding.barCode.doOnTextChanged { text, start, count, after ->
            if(!TextUtils.isEmpty(binding.barCode.text.toString().trim())){
                if(findCNote(binding.barCode.text.toString())){
                    Log.d("barCode",binding.barCode.text.toString())
                    printBarCode()
                }
            }
        }

    }

    private fun setupUI() {
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        binding.rvStickerListRecyclerview.adapter = StickerPrintListAdapter().apply {
            itemClick = { scan ->

            }
        }
        binding.ivDownload.setOnClickListener(this)
        binding.ivPrint.setOnClickListener(this)
        binding.barCode.setOnClickListener(this)
    }

    private fun setObserver() {
        stickerDataViewModel.stickerDataList.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            Log.d(TAG,responseModel.toString())
            cNoteList = responseModel as MutableList<StickerDataResponseModel>
            (binding.rvStickerListRecyclerview.adapter as StickerPrintListAdapter).setItems(
                responseModel,this
            )
        })
    }

    override fun onPostResume() {
        super.onPostResume()

        if(sharedPreference.getValueString("result")?.isNotEmpty() == true){
            var str = sharedPreference.getValueString("result")
            binding.barCode.setText(str)
            /*if(findCNote(str!!)){
                binding.barCode.setText(str)// = str
                //printBarCode()
            }else{
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                     .setTitleText("Wrong Carton Scan")
                    .setContentText(str)
                    .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener {
                        it.dismiss()
                        startScanning()
                    })
                    .show()
            }*/


            sharedPreference.removeValue("result")
        }
    }

    private fun findCNote(str: String): Boolean {
        var value = false
        /*var list = mutableListOf<StickerDataResponseModel>()
        list.addAll(cNoteList)*/
        cNoteList.forEach lit@ {
            if(it.BarCodeNo.trim()==str.trim()){
                Log.d("printDone",it.printDone.toString())
                value =true
                cNote = it
                it.printDone = true
                var index = cNoteList.indexOf(it)
                cNoteList[index] = it
                Log.d("printDone",it.printDone.toString())
                (binding.rvStickerListRecyclerview.adapter as StickerPrintListAdapter).setItems(
                    cNoteList,this
                )
                Log.d("cNoteList",cNoteList.toString())
                return@lit

            }
        }

        return  value
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

    override fun onClick(p0: View?) {
        when(p0?.id){
            binding.ivDownload.id->downloadSticker()
            binding.barCode.id->{
                isCamera = false
            }
            binding.ivPrint.id->{
                //printBarCode()
                isCamera = true
                managePermissions.checkPermissions()
                selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
                startScanning()
            }
        }
    }

    private fun printBarCode() {
        var macAdd = sharedPreference.getValueString(Constant.MAC_ADDRESS)
        //showToast(macAdd.toString())
        Log.d("mac_address",macAdd.toString())
        try {
            TscDll.openport(sharedPreference.getValueString(Constant.MAC_ADDRESS)) //BT
            TscDll.sendcommand("SIZE 76 mm, 50 mm\r\n")
            TscDll.sendcommand("SPEED 4\r\n")
            TscDll.sendcommand("DENSITY 12\r\n")
            TscDll.sendcommand("CODEPAGE UTF-8\r\n")
            TscDll.sendcommand("SET TEAR ON\r\n")
            TscDll.sendcommand("SET GAP 1\r\n")
            TscDll.clearbuffer()
            TscDll.sendcommand("BOX 0,0,866,866,5")
            TscDll.sendcommand("TEXT 100,300,\"ROMAN.TTF\",0,12,12,@1\r\n")
            TscDll.printerfont(15, 160, "5", 270, 1, 1, "MPCL")
            TscDll.barcode(120, 50, "128", 100, 1, 0, 4, 5, cNote?.BarCodeNo)
            TscDll.printerfont(
                10,
                190,
                "2",
                0,
                1,
                1,
                "---------------------------------------------"
            )
            TscDll.printerfont(50, 210, "4", 0, 1, 1, cNote?.CNoteNo)
            TscDll.printerfont(390, 210, "4", 0, 1, 1, cNote?.TotalBox.toString())
            TscDll.printerfont(
                10,
                240,
                "2",
                0,
                1,
                1,
                "---------------------------------------------"
            )
            TscDll.printerfont(30, 260, "4", 0, 1, 1, cNote?.CompRefNo)
            TscDll.printerfont(350, 260, "4", 0, 1, 1, cNote?.Shortcode)
            TscDll.printerfont(10, 310, "3", 0, 1, 1, cNote?.Consignee)
            TscDll.printerfont(10, 360, "3", 0, 1, 1, cNote?.Destination)
            TscDll.printerfont(350, 360, "4", 0, 1, 1, cNote?.Location.toString())
            TscDll.printlabel(1, 1)
            TscDll.closeport(5000)
        } catch (ex: Exception) {
        }

        /*SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(cNote?.BarCodeNo)
            .setContentText("Barcode print done")
            .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener {
                it.dismiss()


                binding.barCode.setText("")
            })
            .show()*/
        showToast("BarCode : ${cNote?.BarCodeNo} \n Barcode Print Done")
        binding.barCode.setText("")
        if(isCamera) startScanning()

    }

    private fun downloadSticker() {
        val mScanDocDataBody = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "DOCNUMBER" to binding.etTripSheetNo.text.toString()
        )
        showDialog()
        Log.d(TAG,mScanDocDataBody.toString())
        stickerDataViewModel.getStickerDataList(mScanDocDataBody)
    }
}