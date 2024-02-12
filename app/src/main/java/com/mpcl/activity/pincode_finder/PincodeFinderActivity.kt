package com.mpcl.activity.pincode_finder

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.mpcl.R
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.databinding.ActivityPincodeFinderBinding
import com.mpcl.viewmodel.pincodeFinderViewModel.PinCodeFinderRepository
import com.mpcl.viewmodel.pincodeFinderViewModel.PinCodeFinderViewModel
import com.mpcl.viewmodel.pincodeFinderViewModel.PinCodeFinderViewModelFactory

class PincodeFinderActivity : BaseActivity() {
    private lateinit var binding:ActivityPincodeFinderBinding
    private lateinit var pinCodeFinderRepository: PinCodeFinderRepository
    private lateinit var pinCodeFinderViewModel: PinCodeFinderViewModel
    private lateinit var pinCodeFinderViewModelFactory: PinCodeFinderViewModelFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPincodeFinderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        pinCodeFinderRepository = PinCodeFinderRepository()
        pinCodeFinderViewModelFactory = PinCodeFinderViewModelFactory(pinCodeFinderRepository)
        pinCodeFinderViewModel =
            ViewModelProvider(this, pinCodeFinderViewModelFactory).get(PinCodeFinderViewModel::class.java)
        setObserver()
        binding.find.setOnClickListener {
            val body = mapOf<String, String>(
                "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                "PINCODE" to binding.pincodeFinder.text.toString()
            )
            pinCodeFinderViewModel.getPinCodeFinder(body)
            showDialog()
        }

    }

    private fun setObserver() {
        pinCodeFinderViewModel.pinCodeFinderResponse.observe(this) {
            hideDialog()
            if (it.isNotEmpty() && it[0].Response==null) {
                binding.tableLayout.visibility = View.VISIBLE

                it[0].run {
                    if(Disabled.equals("No"))
                        binding.trActive.setBackgroundColor(resources.getColor(R.color.light_red))
                    else binding.trActive.setBackgroundColor(resources.getColor(R.color.light_green))
                    binding.tvDistName.text = DistName
                    binding.tvDelBranch.text = DelBranch
                    binding.tvDelTat.text = DelTat
                    binding.tvCityName.text = CityName
                    binding.tvDisabled.text = Disabled
                    binding.tvStatName.text = StatName
                    binding.tvOda.text = Oda
                    binding.tvPincode.text = Pincode
                    binding.tvDistance.text = Distance
                }
            }else binding.tableLayout.visibility = View.GONE
        }
    }
}