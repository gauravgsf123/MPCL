package com.mpcl.activity.operation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.mpcl.R
import com.mpcl.adapter.StockCheckingListAdapter
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.database.ObjectBox
import com.mpcl.database.StockCheckingDB
import com.mpcl.database.StockCheckingDB_
import com.mpcl.databinding.ActivityStockCheckingBinding
import com.mpcl.util.qr_scanner.QRcodeScanningActivity
import com.mpcl.viewmodel.stockCheckingViewModel.StockCheckingRepository
import com.mpcl.viewmodel.stockCheckingViewModel.StockCheckingViewModel
import com.mpcl.viewmodel.stockCheckingViewModel.StockCheckingViewModelFactory
import io.objectbox.Box
import org.json.JSONArray
import org.json.JSONObject

class StockCheckingActivity : BaseActivity() {
    private lateinit var binding: ActivityStockCheckingBinding
    private val REQUEST_CAMERA_CAPTURE = 100
    private lateinit var managePermissions: ManagePermissions
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private val permissionList = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var stockCheckingViewModel: StockCheckingViewModel
    private lateinit var stockCheckingRepository: StockCheckingRepository
    private lateinit var stockCheckingViewModelFactory: StockCheckingViewModelFactory

    private var scanList : MutableList<String> = mutableListOf()
    private var stockCheckBox: Box<StockCheckingDB>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockCheckingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stockListRecyclerview.adapter = StockCheckingListAdapter().apply {
            itemClick = { scan ->

            }
        }

        stockCheckBox = ObjectBox.boxStore.boxFor(StockCheckingDB::class.java)
        /*if (stockCheckBox?.all?.size!! > 0 ) {

            binding.constraintLayout.visibility = View.VISIBLE
            binding.save.visibility = View.VISIBLE
            binding.totalScan.text = "Total Number of Scan : " + stockCheckBox?.all?.size!!
            val stockData = stockCheckBox?.all
            stockData?.let {
                (binding.stockListRecyclerview.adapter
                        as StockCheckingListAdapter)
                    .setItems(it)
            }
        }else{
            binding.constraintLayout.visibility = View.GONE
            binding.save.visibility = View.GONE
        }*/

        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)

        binding.boxNo.setOnClickListener {
            startScanning()
        }






        stockCheckingRepository =  StockCheckingRepository()
        stockCheckingViewModelFactory = StockCheckingViewModelFactory(stockCheckingRepository)
        stockCheckingViewModel = ViewModelProvider(this, stockCheckingViewModelFactory).get(
            StockCheckingViewModel::class.java)
        stockCheckingViewModel.stockCheckingResponse.observe(this, androidx.lifecycle.Observer { it ->
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                Log.d(TAG, responseModel[0].Response.toString())

                stockCheckBox?.removeAll()
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Success")
                    .setContentText("Data successfully uploaded on server")
                    .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener {sweetAlert ->
                        sweetAlert.dismiss()
                        finish()
                    })
                    .show()

            } else {
                showError(
                    getString(R.string.opps),
                    "Some thing wrong"
                )
            }


        })

        binding.save.setOnClickListener {
            val finalJSON = getJSon(stockCheckBox?.all!!).toString()
            Log.d("JSON",finalJSON)
            stockCheckingViewModel.uploadStockChecking(finalJSON)
            scanList.clear()


        }

        //(binding.stockListRecyclerview.adapter as StockCheckingListAdapter).setItems(scanList)
    }

    override fun onPostResume() {
        super.onPostResume()

        if(sharedPreference.getValueString("result")?.isNotEmpty() == true) {
            var str = sharedPreference.getValueString("result")
            Log.d(TAG, str!!)
            binding.boxNo.setText(str)
            if (stockCheckBox?.all?.size!! > 0) {

                var stockData = stockCheckBox?.query()?.equal(StockCheckingDB_.bar_code, str)?.build()?.findFirst()
                if (stockData!=null) {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Barcode already scan")
                        .setContentText(str)
                        .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener {
                            it.dismiss()
                            startScanning()
                        })
                        .show()
                } else {
                    str?.let { scanList.add(it) }
                    stockCheckBox?.put(str?.let { it1 -> getData(it1) })
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Barcode")
                        .setContentText(str)
                        .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener {

                            it.dismiss()
                            startScanning()
                        })
                        .show()
                }

            } else {

                var str = sharedPreference.getValueString("result")
                str?.let { scanList.add(it) }
                stockCheckBox?.put(str?.let { it1 -> getData(it1) })
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Barcode")
                    .setContentText(str)
                    .setConfirmButton("ok",SweetAlertDialog.OnSweetClickListener {

                        it.dismiss()
                        startScanning()
                    })
                    .show()
            }
            //stockCheckBox!!.notifyAll()



            sharedPreference.removeValue("result")

            /*for (data in scanList) {
                //if(!isExist(data.RouteID)){
                //Log.d(TAG, getEkartData(data).srno.toString())
                stockCheckBox?.put()
            }*/
        }
        setRecylatView()
    }

    private fun setRecylatView() {
        if (stockCheckBox?.all?.size!! > 0 ) {
            Log.d(TAG,stockCheckBox?.all?.size.toString())
            binding.constraintLayout.visibility = View.VISIBLE
            binding.save.visibility = View.VISIBLE
            binding.totalScan.text = "Total Number of Scan : " + stockCheckBox?.all?.size!!
            (binding.stockListRecyclerview.adapter as StockCheckingListAdapter).setItems(
                stockCheckBox?.all!!
            )
            //binding.stockListRecyclerview.adapter.notifyDataSetChanged()
        }else{
            binding.constraintLayout.visibility = View.GONE
            binding.save.visibility = View.GONE
        }
    }

    fun getData(item: String): StockCheckingDB {
        val stockData = StockCheckingDB()
        stockData.bar_code = item


        return stockData
    }

    fun getJSon(scanCode: List<StockCheckingDB>): JSONObject {
        val HeaderObj = JSONObject()
        val jsonArrayT1 = JSONArray()
        var atten: JSONObject? = null

        for (value in scanCode) {

            atten = JSONObject()
            atten.put("scan_code", value.bar_code)
            jsonArrayT1.put(atten)

        }


        HeaderObj.put("CID", sharedPreference.getValueString(Constant.COMPANY_ID))
        HeaderObj.put("BID", sharedPreference.getValueString(Constant.BID))
        HeaderObj.put("EMPNO", sharedPreference.getValueString(Constant.EMP_NO))
        HeaderObj.put("MOBILENO", sharedPreference.getValueString(Constant.MOBILE))
        HeaderObj.put("IMEINO", getDeviceIMEIId(this).toString())
        HeaderObj.put("data", jsonArrayT1)
        //Log.d(TAG, "json_data : " + HeaderObj)
        return HeaderObj
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