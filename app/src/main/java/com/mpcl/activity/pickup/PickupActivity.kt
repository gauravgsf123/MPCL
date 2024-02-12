package com.mpcl.activity.pickup

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mpcl.R
import com.mpcl.adapter.PickupListAdapter
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.databinding.ActivityPickupBinding
import com.mpcl.model.PickupResponseModel
import com.mpcl.viewmodel.pickViewModel.PickupRepository
import com.mpcl.viewmodel.pickViewModel.PickupViewModel
import com.mpcl.viewmodel.pickViewModel.PickupViewModelFactory

class PickupActivity : BaseActivity() {
    private lateinit var binding: ActivityPickupBinding
    private lateinit var pickupRepository: PickupRepository
    private lateinit var pickupViewModel: PickupViewModel
    private lateinit var pickupViewModelFactory: PickupViewModelFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pickupRepository = PickupRepository()
        pickupViewModelFactory = PickupViewModelFactory(pickupRepository)
        pickupViewModel =
            ViewModelProvider(this, pickupViewModelFactory).get(PickupViewModel::class.java)
        setObservar()

        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        var body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "EMPNO" to sharedPreference.getValueString(Constant.EMP_NO)!!,
            "MOBILENO" to sharedPreference.getValueString(Constant.MOBILE)!!
        )
        pickupViewModel.pickupRequest(body)
        showDialog(true)
    }

    private fun setObservar() {
            pickupViewModel.pickupResponse.observe(this, Observer {
                hideDialog()
                var response = it ?: return@Observer
                if(response[0].Response!="Failed")
                binding.rvPickupList.adapter = PickupListAdapter(response as ArrayList<PickupResponseModel>)
                else Toast.makeText(this,"List empty", Toast.LENGTH_SHORT).show()
            })
    }

}