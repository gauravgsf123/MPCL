package com.mpcl.activity.pickup

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.mpcl.R
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.databinding.ActivityPickupUpdateBinding
import com.mpcl.viewmodel.pickViewModel.PickupRepository
import com.mpcl.viewmodel.pickViewModel.PickupViewModel
import com.mpcl.viewmodel.pickViewModel.PickupViewModelFactory
import org.json.JSONObject
import kotlin.collections.ArrayList

class PickupUpdateActivity : BaseActivity() {
    private lateinit var binding :ActivityPickupUpdateBinding
    private lateinit var pickupRepository: PickupRepository
    private lateinit var pickupViewModel: PickupViewModel
    private lateinit var pickupViewModelFactory: PickupViewModelFactory
    private var pickupType:String=""
    private var rejectReason:String=""
    private lateinit var requestNo:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickupUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestNo = intent.getStringExtra("RequestNo")!!
        pickupRepository = PickupRepository()
        pickupViewModelFactory = PickupViewModelFactory(pickupRepository)
        pickupViewModel = ViewModelProvider(this, pickupViewModelFactory).get(PickupViewModel::class.java)
        //qImageView.setBackgroundResource(R.drawable.thumbs_down);
        binding.topBar.ivHome.setImageResource(R.drawable.ic_arrow_back)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }

        val pickupStatusOption = resources.getStringArray(R.array.pickup_update_option)
        val pickupStatusOptionAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, pickupStatusOption)
        binding.type.setAdapter(pickupStatusOptionAdapter)
        //var pickupNotDoneReason = arrayListOf("Select","Consignment Not Ready","Give to Compititor","Refuse by Consignor","Document Not Ready","Delay by Branch/PDA","Other")


        var body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!
        )
        pickupViewModel.pickupTypeReason(body)
        showDialog()

        binding.type.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                when(parent.getItemAtPosition(position)){
                    "Pickup Done"->{
                        pickupType = "Done"
                        binding.textInputReason.visibility = View.GONE
                        binding.textInputLayoutCNoteNo.visibility = View.VISIBLE
                    }
                    "Pickup Not Done"->{
                        pickupType = "Not"
                        binding.textInputReason.visibility = View.VISIBLE
                        binding.textInputLayoutCNoteNo.visibility = View.GONE
                    }
                    else -> pickupType = ""
                }
            }

        binding.reason.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                rejectReason = (parent.getItemAtPosition(position).toString())
            }

        binding.save.setOnClickListener {
            if(validateData()){
                var cNote = binding.CNoteNo.text.toString()
                when(pickupType){
                    "Done"->rejectReason=""
                    "Not"->cNote=""
                }
                showDialog()
                val jsonObject = JSONObject()
                jsonObject.put("Response","Success")
                jsonObject.put("RequestNo",requestNo)
                jsonObject.put("CNoteNo",cNote)
                jsonObject.put("ContactName",binding.contactName.text.toString())
                jsonObject.put("ContactNo",binding.ContactNo.text.toString())
                jsonObject.put("Reason",rejectReason)
                jsonObject.put("Status",pickupType)

                Log.d("JSON",jsonObject.toString())
                var body = mapOf<String, String>(
                    "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                    "BID" to sharedPreference.getValueString(Constant.BID)!!,
                    "DATASTR" to jsonObject.toString()
                )
                pickupViewModel.pickupSave(body)
            }
        }

        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }

        setObserver()

    }

    private fun validateData():Boolean{
        binding.let {
            if(pickupType.isEmpty()){
                Toast.makeText(this,"Please select Pickup type", Toast.LENGTH_SHORT).show()
                return false
            }
            else if(TextUtils.isEmpty(it.contactName.text.toString())) {
                Toast.makeText(this,"Please enter Contact Person Name.", Toast.LENGTH_SHORT).show()
                return false
            }
            else if(TextUtils.isEmpty(it.ContactNo.text.toString())) {
                Toast.makeText(this,"Please enter Contact Person Number.", Toast.LENGTH_SHORT).show()
                return false
            }
            else if(pickupType=="Done" && TextUtils.isEmpty(it.CNoteNo.text.toString())) {
                Toast.makeText(this,"Please enter C Note Number", Toast.LENGTH_SHORT).show()
                return false
            }
            else if(pickupType=="Not" && rejectReason.isEmpty()) {
                Toast.makeText(this,"Please select Reject Reason", Toast.LENGTH_SHORT).show()
                return false
            }
            else return true
        }
    }

    private fun setObserver() {
        pickupViewModel.pickupReasonResponseModel.observe(this, androidx.lifecycle.Observer {
            if(it.isNotEmpty()){
                hideDialog()
                var arr = ArrayList<String>()
                it.forEach {str->
                    arr.add(str.Types!!)
                }
                val pickupNotDoneReasonAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, arr)
                binding.reason.setAdapter(pickupNotDoneReasonAdapter)
            }
        })

        pickupViewModel.savePickupResponse.observe(this, androidx.lifecycle.Observer {
            hideDialog()
            //if(it.isNotEmpty() && it[0].equals("Success")) {
            if (it[0].Response.toString() == "Success") {
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getString(R.string.success))
                    .setContentText(getString(R.string.congrats_data_successful_uploaded))
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismiss()
                        finish()
                    }
                    .show()
            }
            //}
        })
    }
}