package com.mpcl.activity.operation.box_wise_scan

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.mpcl.activity.operation.boxpacking.BoxPackingAdapter
import com.mpcl.activity.operation.boxpacking.BoxPackingRepository
import com.mpcl.activity.operation.boxpacking.BoxPackingViewModel
import com.mpcl.activity.operation.boxpacking.BoxPackingViewModelFactory
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.databinding.ActivityBoxWiseScanBinding
import com.mpcl.util.qr_scanner.QRcodeScanningActivity

class BoxWiseScanActivity : BaseActivity() {
    private lateinit var binding:ActivityBoxWiseScanBinding
    private val REQUEST_CAMERA_CAPTURE = 100
    private lateinit var managePermissions: ManagePermissions
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )
    private var isCamera = false
    private var barCodeList = ArrayList<String>()
    private var type = "IN"
    private lateinit var viewModel: BoxWiseScanViewModel
    private lateinit var repository: BoxWiseScanRepository
    private lateinit var viewModelFactory: BoxWiseScanViewModelFactory
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoxWiseScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        repository = BoxWiseScanRepository()
        viewModelFactory = BoxWiseScanViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BoxWiseScanViewModel::class.java)
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        binding.boxPackingListRecyclerview.adapter = BoxWiseScanAdapter()
        binding.ivCamera.setOnClickListener {
            isCamera = true
            managePermissions.checkPermissions()
            selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
            startScanning()
        }
        binding.btnSubmit.setOnClickListener {
            validate()
        }
        binding.btnNewScan.setOnClickListener {
            barCodeList.clear()
            (binding.boxPackingListRecyclerview.adapter as BoxWiseScanAdapter).setItems(barCodeList,this)
        }

        binding?.rgVechicle.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radio: RadioButton = findViewById(checkedId)
                when(checkedId){
                    binding.rbLoading.id->type="IN"
                    binding.rbUnloading.id->type="OUT"
                }
            })

        binding.scanBarCode.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            val inputMethod: InputMethodManager =
                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethod != null) {
                inputMethod.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        setObservar()

    }

    private fun setObservar(){
        viewModel.boxPackingResponseModel.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if(responseModel[0].Response=="Failed") {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Failed")
                    .setContentText("Try Again")
                    .setConfirmButton("Retry", SweetAlertDialog.OnSweetClickListener { sweetAlert ->
                        sweetAlert.dismiss()
                        binding.btnSubmit.performClick()
                        //finish()
                    })
                    .setCancelButton("Cancel", SweetAlertDialog.OnSweetClickListener { sweetAlert->
                        sweetAlert.dismiss()
                    })
                    .show()
            }else{
                clearAllData()
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Success")
                    .setContentText("Data successfully uploaded on server")
                    .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { sweetAlert ->
                        sweetAlert.dismiss()
                        //finish()
                    })
                    .show()
            }
        })
    }

    private fun clearAllData(){
        barCodeList.clear()
        (binding.boxPackingListRecyclerview.adapter as BoxPackingAdapter).setItems(barCodeList,this)
        binding.run {
            vehicleNo.setText("")
            scanBarCode.setText("")
            scanBarCode.requestFocus()
        }

    }

    private fun validate(){
        if(TextUtils.isEmpty(binding?.vehicleNo.text.toString())){
            Toast.makeText(this,"Please enter vehicle number",Toast.LENGTH_SHORT).show()
        }else{
            showDialog()
            var dataRequest = ArrayList<BoxWiseScanRequestModel.Data>()
            for (value in barCodeList) {
                dataRequest.add(BoxWiseScanRequestModel.Data(value))
            }

            var requestModel = BoxWiseScanRequestModel(
                sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                sharedPreference.getValueString(Constant.BID)!!,
                binding?.vehicleNo.text.toString(),
                        type,
                dataRequest!!
            )
            //var jsonData = Gson().toJson(vecicleloadRequst)
            viewModel.boxScan(requestModel)
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if (sharedPreference.getValueString("result")!!.isNotEmpty()) {
            var str = sharedPreference.getValueString("result")
            Log.d(TAG, str!!)
            binding.scanBarCode.setText(str)
            sharedPreference.removeValue("result")
            barCodeList.add(str)
            (binding.boxPackingListRecyclerview.adapter as BoxWiseScanAdapter).setItems(barCodeList,this)
            Toast.makeText(this,"${binding.scanBarCode.text.toString()} Added", Toast.LENGTH_SHORT).show()
            binding.scanBarCode.requestFocus()
            binding.scanBarCode.setText("")
            //if(isCamera) startScanning()
        }
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

    private fun openCameraWithScanner() {
        QRcodeScanningActivity.start(this, selectedScanningSDK)
    }
}