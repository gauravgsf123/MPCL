package com.mpcl.activity.operation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.mpcl.R
import com.mpcl.adapter.VechileLoadAdapter
import com.mpcl.database.ObjectBox
import com.mpcl.database.VechileData
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.database.*
import com.mpcl.databinding.ActivityVehicleLoadUploadBinding
import com.mpcl.util.qr_scanner.QRcodeScanningActivity
import com.mpcl.viewmodel.VehicleLoanUnloadViewModel.VechileLoanUnloadRepository
import com.mpcl.viewmodel.VehicleLoanUnloadViewModel.VechileLoanUnloadViewModel
import com.mpcl.viewmodel.VehicleLoanUnloadViewModel.VechileLoanUnloadViewModelFactory
import io.objectbox.Box
import org.json.JSONArray
import org.json.JSONObject

class VehicleLoadUploadActivity : BaseActivity() {
    private lateinit var binding: ActivityVehicleLoadUploadBinding

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

    private var valueList : MutableList<String> = mutableListOf()
    private var nameList : MutableList<String> = mutableListOf()
    private lateinit var docType :String
    private var enableTrue :Boolean = false
    private var callOneTime :Boolean = false
    private var vechileDataBox: Box<VechileData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehicleLoadUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        vechileLoanUnloadRepository = VechileLoanUnloadRepository()
        vechileLoanUnloadViewModelFactory = VechileLoanUnloadViewModelFactory(vechileLoanUnloadRepository)
        vechileLoanUnloadViewModel = ViewModelProvider(this,vechileLoanUnloadViewModelFactory).get(
            VechileLoanUnloadViewModel::class.java)
        binding.stockListRecyclerview.adapter = VechileLoadAdapter().apply {
            itemClick = { scan ->

            }
        }
        vechileDataBox = ObjectBox.boxStore.boxFor(VechileData::class.java)
        enableTrue = sharedPreference.getValueBoolean(Constant.VECHICLE_ENABLE,false)
        if(enableTrue){
            binding.type.isEnabled = false
            //binding.documentNo.isEnabled = false
            enableDocument(false)
            binding.save.visibility = View.GONE
            binding.textInputLayoutBoxNo.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE
            binding.type.setText(sharedPreference.getValueString(Constant.VECHICLE_POSITION)!!)
            val mScanDocDataBody = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "DOCNUMBER" to getDocumentNo(),
            "DOCTYPE" to sharedPreference.getValueString(Constant.DOCTYPE)!!
            )
            showDialog()
            vechileLoanUnloadViewModel.scanDocTotal(mScanDocDataBody)
        }else{
            showDialog()
            val body = mapOf<String, String>(
                "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!
            )
            vechileLoanUnloadViewModel.getDocTypeList(body)
            binding.type.isEnabled = true
            //binding.documentNo.isEnabled = true
            enableDocument(true)
            binding.save.visibility = View.VISIBLE
            binding.textInputLayoutBoxNo.visibility = View.GONE
            binding.linearLayout.visibility = View.GONE
        }

        binding.boxNo.setOnClickListener {
            startScanning()
        }
        binding.submit.setOnClickListener {
            val finalJSON = getJSon(vechileDataBox?.all!!)
            Log.d("JSON",finalJSON.toString())
            vechileLoanUnloadViewModel.uploadVehicleScan(finalJSON)
        }

        /*showDialog()
        val body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!
        )
        vechileLoanUnloadViewModel.getDocTypeList(body)*/





        vechileLoanUnloadViewModel.docTypeListResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                //responseModel[0].branch?.let { it1 -> Log.d(TAG, it1) }
                for (branch in responseModel){
                    valueList.add(branch.Value!!)
                    nameList.add(branch.Name!!)
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

        vechileLoanUnloadViewModel.scanDocDataResponse.observe(this, Observer {
            hideDialog()
            Log.d(TAG,it.toString())
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
               if(responseModel[0].Message.isNullOrBlank()){
                   if(!callOneTime){
                       callScanTotalAPI()
                       callOneTime = true
                   }
               }else{
                   SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                       .setTitleText(responseModel[0].Response)
                       .setContentText(responseModel[0].Message)
                       .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener {dialog->
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
        })

        vechileLoanUnloadViewModel.scanDocTotalResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                //if(!enableTrue){
                    binding.type.isEnabled = false
                    //binding.documentNo.isEnabled = false
                    enableDocument(false)
                    enableTrue = true
                    binding.save.visibility = View.GONE
                    binding.textInputLayoutBoxNo.visibility = View.VISIBLE
                    binding.linearLayout.visibility = View.VISIBLE
                    sharedPreference.save(Constant.VECHICLE_ENABLE,enableTrue)
                    binding.totalBox.setText(responseModel[0].TotalBox.toString())
                    binding.scanBox.setText(responseModel[0].ScanBox.toString())

               // }
                Log.d(TAG,responseModel.toString())


            } else {
                showError(
                    getString(R.string.opps),
                    "Some thing wrong"
                )
            }
        })

        vechileLoanUnloadViewModel.uploadVehicleScanResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            Log.d(TAG,responseModel.toString())
            vechileDataBox?.removeAll()
            setRecylatView()
            binding.type.isEnabled = false
            //binding.documentNo.isEnabled = false
            enableDocument(false)
            enableTrue = false
            binding.save.visibility = View.VISIBLE
            binding.submit.visibility = View.GONE
            binding.textInputLayoutBoxNo.visibility = View.GONE
            binding.linearLayout.visibility = View.GONE
            sharedPreference.save(Constant.VECHICLE_ENABLE,enableTrue)
            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Data successfully uploaded on server")
                .setConfirmButton("ok", SweetAlertDialog.OnSweetClickListener {sweetAlert ->
                    sweetAlert.dismiss()
                    finish()
                })
                .show()
        })




        binding.type.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selected = parent.getItemAtPosition(position)
                docType = valueList[position]
            sharedPreference.save(Constant.VECHICLE_POSITION,nameList[position])
            sharedPreference.save(Constant.DOCTYPE,docType)
                 Log.d(TAG, docType!!)
            when(docType!!){
                "PRS"->{showHide(true);binding.documentNo3.setText("PR")}
                "MFIN"->{
                    showHide(false)
                    }
                "MFOUT"->{
                    showHide(true);binding.documentNo3.setText("MF")
                }
                "DRS","DRS"->{showHide(true);binding.documentNo3.setText("DR")}
            }

        }



        binding.save.setOnClickListener {
            val mScanDocDataBody = mapOf<String, String>(
                "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                "BID" to sharedPreference.getValueString(Constant.BID)!!,
                "DOCNUMBER" to getDocumentNo(),
                "DOCTYPE" to docType
            )
            showDialog()
            callOneTime = false
            Log.d(TAG,mScanDocDataBody.toString())
            vechileLoanUnloadViewModel.scanDocData(mScanDocDataBody)
        }

        binding.reset.setOnClickListener {
            vechileDataBox?.removeAll()
            setRecylatView()
            binding.type.isEnabled = false
            //binding.documentNo.isEnabled = false
            enableDocument(true)
            enableTrue = false
            binding.save.visibility = View.VISIBLE
            binding.submit.visibility = View.GONE
            binding.textInputLayoutBoxNo.visibility = View.GONE
            binding.linearLayout.visibility = View.GONE
            sharedPreference.save(Constant.VECHICLE_ENABLE,enableTrue)
        }






        /*val traderType  = listOf("Buyer", "Seller", "Buyer & Seller ")
        val traderTypeAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, traderType)
        binding.type.setAdapter(traderTypeAdapter)*/
    }

    private fun showHide(isShow:Boolean){
        if(isShow){
            binding.documentNo1.visibility = View.VISIBLE
            binding.documentNo2.visibility = View.VISIBLE
            binding.documentNo3.visibility = View.VISIBLE
        }else{
            binding.documentNo1.visibility = View.GONE
            binding.documentNo2.visibility = View.GONE
            binding.documentNo3.visibility = View.GONE
        }
    }

    fun getJSon(scanCode: List<VechileData>): Map<String,String> {
        val HeaderObj = JSONObject()
        val jsonArrayT1 = JSONArray()
        var atten: JSONObject? = null
        val jsonObj = JSONObject()

        for (value in scanCode) {
            atten = JSONObject()
            atten.put("scan_code", value.bar_code)
            jsonArrayT1.put(atten)
        }

        jsonObj.put("CID",sharedPreference.getValueString(Constant.COMPANY_ID)!!)
        jsonObj.put("data",jsonArrayT1.toString())

        val body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "DOCNUMBER" to getDocumentNo(),
            "DOCTYPE" to sharedPreference.getValueString(Constant.DOCTYPE)!!,
            "EMPNO" to sharedPreference.getValueString(Constant.EMP_NO)!!,
            "DATASTR" to jsonObj.toString()
        )

        //https://www.maxpacific.org/Magix/rest/operation/mVehicleScan.htm?CID=MAXPACIFIC&BID=6&DOCNUMBER=202122/GGN/MF0001209&DOCTYPE=MFOUT&EMPNO=MAX0111&DATASTR={%22CID%22:%22MAXPACIFIC%22,%22data%22:[{%22scan_code%22:%225012345678900%22},{%22scan_code%22:%22036000291452%22},{%22scan_code%22:%22036000291452%22}]}


        /*HeaderObj.put("CID", sharedPreference.getValueString(Constant.COMPANY_ID))
        HeaderObj.put("BID", sharedPreference.getValueString(Constant.BID))
        HeaderObj.put("EMPNO", sharedPreference.getValueString(Constant.EMP_NO))
        HeaderObj.put("MOBILENO", sharedPreference.getValueString(Constant.MOBILE))
        HeaderObj.put("IMEINO", getDeviceIMEIId(this).toString())
        HeaderObj.put("data", jsonArrayT1)*/
        //Log.d(TAG, "json_data : " + HeaderObj)
        return body
    }



    override fun onPostResume() {
        super.onPostResume()

        if(sharedPreference.getValueString("result")!!.isNotEmpty()) {
            var str = sharedPreference.getValueString("result")
            Log.d(TAG, str!!)
            binding.boxNo.setText(str)
            if (vechileDataBox?.all?.size!! > 0) {

                var stockData = vechileDataBox?.query()?.equal(VechileData_.bar_code, str)?.build()?.findFirst()
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
                   // str?.let { scanList.add(it) }
                    vechileDataBox?.put(str?.let { it1 -> getData(it1) })
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
                //str?.let { scanList.add(it) }
                vechileDataBox?.put(str?.let { it1 -> getData(it1) })
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
        if (vechileDataBox?.all?.size!! > 0 ) {
            Log.d(TAG,vechileDataBox?.all?.size.toString())
            binding.constraintLayout.visibility = View.VISIBLE
            binding.submit.visibility = View.VISIBLE
            binding.totalScan.text = "Total Number of Scan : " + vechileDataBox?.all?.size!!
            (binding.stockListRecyclerview.adapter as VechileLoadAdapter).setItems(
                vechileDataBox?.all!!
            )
            //binding.stockListRecyclerview.adapter.notifyDataSetChanged()
        }else{
            binding.constraintLayout.visibility = View.GONE
            binding.submit.visibility = View.GONE
        }
    }

    private fun getData(item: String): VechileData {
        val stockData = VechileData()
        stockData.bar_code = item


        return stockData
    }

    private fun callScanTotalAPI(){
        val mScanDocDataBody = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "DOCNUMBER" to getDocumentNo(),
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

    fun enableDocument(enable:Boolean){
        binding.documentNo1.isEnabled = enable
        binding.documentNo2.isEnabled = enable
        binding.documentNo3.isEnabled = enable
        binding.documentNo4.isEnabled = enable
        if(enable) {
            binding.documentNo1.setText("")
            binding.documentNo2.setText("")
            binding.documentNo3.setText("")
            binding.documentNo4.setText("")
        }
    }

    fun getDocumentNo():String{
        var documentno = ""
        when(docType!!){
            "PRS"-> documentno= "${binding.documentNo1.text.toString()}/${binding.documentNo2.text.toString()}/${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            "MFIN"->{
                documentno= "${binding.documentNo4.text.toString()}"
            }
            "MFOUT"->{
                documentno= "${binding.documentNo1.text.toString()}/${binding.documentNo2.text.toString()}/${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
            }
            "DRS","DRS"-> documentno= "${binding.documentNo1.text.toString()}/${binding.documentNo2.text.toString()}/${binding.documentNo3.text.toString()}${binding.documentNo4.text.toString()}"
        }
       return documentno
    }
}