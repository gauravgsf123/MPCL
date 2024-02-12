package com.mpcl.activity.barcode_setting

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.tscdll.TSCActivity
import com.mpcl.R
import com.mpcl.adapter.StickerPrintListAdapter
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.databinding.ActivityPickupScanBinding
import com.mpcl.databinding.ActivityStickerPrintBinding
import com.mpcl.model.StickerDataResponseModel
import com.mpcl.util.qr_scanner.QRcodeScanningActivity
import com.mpcl.viewmodel.stickerDataViewModel.StickerDataRepository
import com.mpcl.viewmodel.stickerDataViewModel.StickerDataViewModel
import com.mpcl.viewmodel.stickerDataViewModel.StickerDataViewModelFactory
import java.lang.Exception

class PickupScanActivity : BaseActivity(), View.OnClickListener {
    var TscDll = TSCActivity()
    private lateinit var binding:ActivityPickupScanBinding
    private lateinit var stickerDataRepository: StickerDataRepository
    private lateinit var stickerDataViewModel: StickerDataViewModel
    private lateinit var stickerDataViewModelFactory: StickerDataViewModelFactory
    private var cNoteList= mutableListOf<StickerDataResponseModel>()
    private var cNote : StickerDataResponseModel?=null
    private lateinit var managePermissions: ManagePermissions
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private var isCamera = false
    private var scanCount = 0
    private val REQUEST_CAMERA_CAPTURE = 1002
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickupScanBinding.inflate(layoutInflater)
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

        binding.barCode.showSoftInputOnFocus = false

        binding.barCode.doOnTextChanged { text, start, count, after ->
            if(!TextUtils.isEmpty(binding.barCode.text.toString().trim())){
                if(findCNote(binding.barCode.text.toString())){
                    ++scanCount
                    binding.etScanHu.setText(scanCount.toString())
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(getString(R.string.success))
                        .setContentText("Found Done")
                        .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { dialog ->
                            dialog.dismiss()
                        })
                        .show()
                }else{
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.success))
                        .setContentText("Not matched")
                        .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { dialog ->
                            dialog.dismiss()
                        })
                        .show()
                }
            }


        }
    }

    private fun setObserver() {
        stickerDataViewModel.stickerDataList.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            Log.d(TAG,responseModel.toString())
            cNoteList = responseModel as MutableList<StickerDataResponseModel>
            binding.etTotalHu.setText(cNoteList.size.toString())
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

            sharedPreference.removeValue("result")
        }
    }

    private fun findCNote(str: String): Boolean {
        var value = false
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