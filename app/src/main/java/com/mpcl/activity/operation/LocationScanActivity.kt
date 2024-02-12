package com.mpcl.activity.operation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.mpcl.R
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.databinding.ActivityLocationScanBinding
import com.mpcl.util.qr_scanner.QRcodeScanningActivity
import com.mpcl.viewmodel.locationScanViewModel.LocationScanRepository
import com.mpcl.viewmodel.locationScanViewModel.LocationScanViewModel
import com.mpcl.viewmodel.locationScanViewModel.LocationScanViewModelFactory


class LocationScanActivity : BaseActivity() {
    private lateinit var binding: ActivityLocationScanBinding
    private val REQUEST_CAMERA_CAPTURE = 100
    private lateinit var managePermissions: ManagePermissions
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )

    private lateinit var scanLocationViewModel: LocationScanViewModel
    private lateinit var scanLocationRepository: LocationScanRepository
    private lateinit var scanLocationViewModelFactory: LocationScanViewModelFactory
    private var branchList : MutableList<String> = mutableListOf()
    private var branchCode : MutableList<String> = mutableListOf()
    private var bid :String?=null
    private var isCamera = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)

        binding.ivCamera.setOnClickListener {
            isCamera = true
            managePermissions.checkPermissions()
            selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
            startScanning()
        }

        binding.barCode.setOnClickListener { isCamera = false }

        //val traderType  = listOf("Noida", "Delhi", "Patna")
//        val traderTypeAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, traderType)
//        binding.location.setAdapter(traderTypeAdapter)



        scanLocationRepository =  LocationScanRepository()
        scanLocationViewModelFactory = LocationScanViewModelFactory(scanLocationRepository)
        scanLocationViewModel = ViewModelProvider(this, scanLocationViewModelFactory).get(
            LocationScanViewModel::class.java)
        scanLocationViewModel.branchListResponse.observe(this, androidx.lifecycle.Observer { it ->
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                //responseModel[0].branch?.let { it1 -> Log.d(TAG, it1) }
                for (branch in responseModel){
                    branchList.add(branch.branch!!)
                    branchCode.add(branch.bid!!)
                }
                val traderTypeAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, branchList)
                binding.branchName.setAdapter(traderTypeAdapter)



            } else {
                showError(
                    getString(R.string.opps),
                    "Some thing wrong"
                )
            }
        })
        val body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!
        )
        scanLocationViewModel.getBranchList(body)

        binding.branchName.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val selected = parent.getItemAtPosition(position)
            bid = branchCode[position]
            Log.d(TAG,branchCode[position].toString()+binding.branchName.text.toString())

        }

        binding.save.setOnClickListener {
            showDialog()
            bid?.let { it1 ->
                scanLocationViewModel.scanLocation(sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                    sharedPreference.getValueString(Constant.BID)!!,sharedPreference.getValueString(Constant.EMP_NO)!!,binding.barCode.text.toString(),binding.branchName.text.toString(),it1)
            }
        }

        scanLocationViewModel.scanLocationResponse.observe(this, Observer {it->
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(responseModel[0].Message)
                    .setContentText(responseModel[0].Response)
                    .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener { sweetAlert ->
                        sweetAlert.dismiss()
                        if (isCamera) startScanning()
                    })
                    .show()
            }

        })

        binding.barCode.doOnTextChanged { text, start, count, after ->
            if(binding.barCode.text.toString().trim().isNotEmpty()){
                showDialog()
                bid?.let { it1 ->
                    scanLocationViewModel.scanLocation(sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                        sharedPreference.getValueString(Constant.BID)!!,sharedPreference.getValueString(Constant.EMP_NO)!!, binding.barCode.text.toString().trim(),
                        binding.branchName.text.toString(),it1
                    )
                }
                binding.barCode.setText("")
            }
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        if(sharedPreference.getValueString("result")!=null){
            var str = sharedPreference.getValueString("result")

            binding.barCode.setText(str)
            //speakText(ekart?.location)
            sharedPreference.removeValue("result")
        }
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
}