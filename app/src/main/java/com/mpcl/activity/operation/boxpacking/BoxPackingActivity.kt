package com.mpcl.activity.operation.boxpacking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.mpcl.adapter.VechileLoadAdapter
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.database.VehicleListData
import com.mpcl.databinding.ActivityBoxPackingBinding
import com.mpcl.model.VehicleLoadRequest
import com.mpcl.util.qr_scanner.QRcodeScanningActivity

class BoxPackingActivity : BaseActivity() {
    private lateinit var binding:ActivityBoxPackingBinding
    private val REQUEST_CAMERA_CAPTURE = 100
    private lateinit var managePermissions: ManagePermissions
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )
    private var isCamera = false
    private lateinit var viewModel: BoxPackingViewModel
    private lateinit var repository: BoxPackingRepository
    private lateinit var viewModelFactory: BoxPackingViewModelFactory
    private var skuList = ArrayList<String>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoxPackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        repository = BoxPackingRepository()
        viewModelFactory = BoxPackingViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(BoxPackingViewModel::class.java)
        binding.ivCamera.setOnClickListener {
            isCamera = true
            managePermissions.checkPermissions()
            selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
            startScanning()
        }
        binding.pickupNo.onFocusChangeListener =
            View.OnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    val body = mapOf<String, String>(
                        "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                        "PICKUPNO" to binding.pickupNo.text.toString()
                    )
                    viewModel.getPickupExists(body)
                    showDialog()
                    Log.d("hasFocus","$hasFocus")
                } else {
                    Log.d("hasFocus","$hasFocus")

                }
            }
        binding.barCode.setOnClickListener { isCamera = false }
        binding.boxPackingListRecyclerview.adapter = BoxPackingAdapter()
        binding.btnSubmit.setOnClickListener {
            if(validateData()) {
                showDialog()
                var dataRequest = ArrayList<BoxPackingRequestModel.Data>()
                for (value in skuList) {
                    dataRequest.add(BoxPackingRequestModel.Data(value))
                }

                var vecicleloadRequst = BoxPackingRequestModel(
                    sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                    sharedPreference.getValueString(Constant.BID)!!,
                    sharedPreference.getValueString(Constant.EMP_NO)!!,
                    binding.pickupNo.text.toString(),
                    binding.cNoteNumber.text.toString(),
                    binding.sealNo.text.toString(),
                    binding.masterBoxNumber.text.toString(),
                    binding.totalScan.text.toString(),
                    dataRequest!!
                )
                var jsonData = Gson().toJson(vecicleloadRequst)
                viewModel.boxPacking(vecicleloadRequst)
            }
        }

        binding.barCode.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            val inputMethod: InputMethodManager =
                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethod != null) {
                inputMethod.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        binding.barCode.setOnFocusChangeListener {
                view, b ->
            val inputMethod: InputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if(b) inputMethod.hideSoftInputFromWindow(view.windowToken, 0)
        }

        setObserver()
    }

    private fun validateData():Boolean{
        binding.let {
            if(TextUtils.isEmpty(it.pickupNo.text.toString())) {
                Toast.makeText(this,"Please enter Pickup Number.",Toast.LENGTH_SHORT).show()
                return false
            }
            else if(TextUtils.isEmpty(it.cNoteNumber.text.toString())) {
                Toast.makeText(this,"Please enter C Note Number",Toast.LENGTH_SHORT).show()
                return false
            }
            else if(skuList.isEmpty()) {
                Toast.makeText(this,"Please Scan SKU Number",Toast.LENGTH_SHORT).show()
                return false
            }
            else if(TextUtils.isEmpty(it.masterBoxNumber.text.toString())) {
                Toast.makeText(this,"Please enter Master Box Number",Toast.LENGTH_SHORT).show()
                return false
            }
            else if(TextUtils.isEmpty(it.sealNo.text.toString())) {
                Toast.makeText(this,"Please enter Seal Number",Toast.LENGTH_SHORT).show()
                return false
            }
            else return true
        }
    }

    private fun setObserver() {
        viewModel.pickupExistsResponseModel.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if(responseModel[0].Response=="Failed"){
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(responseModel[0].Response)
                    .setContentText(responseModel[0].Message)
                    .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { dialog ->
                        dialog.dismiss()
                    })
                    .show()
            }
        })

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
                    .setCancelButton("Cancel", SweetAlertDialog.OnSweetClickListener {sweetAlert->
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
        skuList.clear()
        (binding.boxPackingListRecyclerview.adapter as BoxPackingAdapter).setItems(skuList,this)
        binding.run {
            totalScan.setText("")
            masterBoxNumber.setText("")
            sealNo.setText("")
            barCode.requestFocus()
        }

    }

    override fun onPostResume() {
        super.onPostResume()
        if (sharedPreference.getValueString("result")!!.isNotEmpty()) {
            var str = sharedPreference.getValueString("result")
            Log.d(TAG, str!!)
            binding.barCode.setText(str)
            sharedPreference.removeValue("result")
            skuList.add(str)
            binding.totalScan.setText(skuList.size.toString())
            (binding.boxPackingListRecyclerview.adapter as BoxPackingAdapter).setItems(skuList,this)
            Toast.makeText(this,"${binding.barCode.text.toString()} SKU Added",Toast.LENGTH_SHORT).show()
            binding.barCode.requestFocus()
            binding.barCode.setText("")
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