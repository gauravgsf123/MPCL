package com.mpcl.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.mpcl.R
import com.mpcl.activity.onboarding.OTPVerifyActivity
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.custom.BoldTextView
import com.mpcl.custom.RegularButton
import com.mpcl.custom.RegularTextView
import com.mpcl.databinding.ActivityLoginBinding
import com.mpcl.viewmodel.registerDevice.RegisterDeviceRepository
import com.mpcl.viewmodel.registerDevice.RegisterDeviceViewModel
import com.mpcl.viewmodel.registerDevice.RegisterDeviceViewModelFactory

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var registerDeviceRepository: RegisterDeviceRepository
    private lateinit var registerDeviceViewModel: RegisterDeviceViewModel
    private lateinit var registerDeviceViewModelFactory: RegisterDeviceViewModelFactory
    lateinit var dialog: AlertDialog
    private lateinit var managePermissions : ManagePermissions
    private val permissionList = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private  var mLocation : Location?=null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private var runningThread = true
    private var handler: Handler? = null
    private var handlerTask: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerDeviceRepository = RegisterDeviceRepository()
        registerDeviceViewModelFactory = RegisterDeviceViewModelFactory(registerDeviceRepository)
        registerDeviceViewModel = ViewModelProvider(this,registerDeviceViewModelFactory).get(
            RegisterDeviceViewModel::class.java)
        binding.submit.setOnClickListener{
            if(managePermissions.checkPermissions())
          validateForm()
        }
        if(sharedPreference.getValueBoolean(Constant.IS_LOGIN,false)){
            startNewActivity(OptionActivity())
            finish()
        }else{
            managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
            managePermissions.checkPermissions()
        }

        registerDeviceViewModel.registerDeviceResponse.observe(this, Observer {
            val responseModel = it ?: return@Observer
            hideDialog()
            Log.e(TAG,responseModel.toString())
            if(responseModel.isNotEmpty()){
                if(responseModel[0].Response?.equals("Success") == true){
                    sharedPreference.save(Constant.COMPANY_ID,binding.companyId.text.toString().trim())
                    sharedPreference.save(Constant.EMP_NO,binding.employeeNo.text.toString().trim())
                    sharedPreference.save(Constant.BID, responseModel[0].Bid!!)
                    sharedPreference.save(Constant.FULL_NAME, responseModel[0].FullName!!)
                    sharedPreference.save(Constant.USER_TYPE, responseModel[0].UserType!!)
                    sharedPreference.save(Constant.COMPANY_ID,binding.companyId.text.toString().trim())
                    sharedPreference.save(Constant.MOBILE,binding.mobile.text.toString().trim())
                    //showPopupMessage(getString(R.string.success),getString(R.string.your_registration_completed),responseModel[0].OtpNo!!)
                    val intent = Intent(this, OTPVerifyActivity::class.java)
                    intent.putExtra(Constant.OTP,responseModel[0].OtpNo!!)
                    startActivity(intent)
                }else{
                    showError(
                        getString(R.string.opps),
                        "Company ID or Employee Code Wrong"
                    )
                }

            }
        })



        //Log.d(TAG,getDeviceIMEIId(this).toString())

    }

    override fun onStop() {
        super.onStop()
        //dialog.dismiss()

    }

    override fun onResume() {
        super.onResume()

        requestNewLocationData()
        Log.e(TAG, "onResume")
        if (managePermissions.checkPermissions()) {
            if (!isLocationEnabled()) {
                showGPSDisabledAlertToUser()
            } else {
                handler = Handler()
                handlerTask = Runnable {
                    handler!!.postDelayed(handlerTask!!, 1000)
                    if (!runningThread) {
                        return@Runnable
                    }
                    if(mLocation!=null){
                        Log.w(TAG,mLocation?.accuracy.toString())
                        if (latitude == 0.0 || longitude == 0.0 || mLocation?.accuracy!! >2000) {
                            requestNewLocationData()
                            if (!isDialogShow()) showDialogForLocation(this)
                        } else {
                            hideDialog()
                            runningThread = false
                        }
                    }else{
                        requestNewLocationData()
                    }

                }
                handlerTask!!.run()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showPopupMessage(titleText: String, messageText: String, otpNo: String){
        var materialAlertDialogBuilder = MaterialAlertDialogBuilder(this, R.style.PauseDialog)
        var customAlertView = LayoutInflater.from(this).inflate(
            R.layout.dialog_no_internet,
            null,
            false
        )
        val title: BoldTextView = customAlertView!!.findViewById(R.id.title)
        val message: RegularTextView = customAlertView!!.findViewById(R.id.message)
        val ok: RegularButton = customAlertView!!.findViewById(R.id.ok)
        materialAlertDialogBuilder.setView(customAlertView)
        materialAlertDialogBuilder.background = getDrawable(R.drawable.card_view)
        title.text = titleText
        message.text = messageText
        ok.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            sharedPreference.save(Constant.COMPANY_ID,binding.companyId.text.toString().trim())
            sharedPreference.save(Constant.MOBILE,binding.mobile.text.toString().trim())
            sharedPreference.save(Constant.IS_LOGIN,true)
            val intent = Intent(this, OTPVerifyActivity::class.java)
            intent.putExtra(Constant.OTP,otpNo)
            startActivity(intent)
            finish()
        })



        dialog = materialAlertDialogBuilder.show()
    }

    fun TextView?.isEmptyField(): Boolean = this?.text?.isEmpty() ?: false

    private fun validateForm() {
        when {
            binding.companyId.isEmpty() -> {
                (binding.companyId).run { error = getString(R.string.required_field);requestFocus() }
            }
            binding.employeeNo.isEmpty() -> {
                (binding.companyId).run { error = getString(R.string.required_field);requestFocus() }
            }
            binding.mobile.isEmpty() -> {
                (binding.textInputMobile).run { error = getString(R.string.required_field);requestFocus() }
            }
            else -> {login()}
        }

    }

    private fun login() {
        showDialog()
        val body = mapOf<String, String>(
            "CID" to binding.companyId.text.toString().trim(),
            "MOBILENO" to binding.mobile.text.toString().trim(),
            "EMPNO" to binding.employeeNo.text.toString().trim(),
            "IMEINO" to getDeviceIMEIId(this).toString(),
            "DeviceId" to "",
            "LATITUDE" to latitude.toString(),
            "LONGITUDE" to longitude.toString()
        )
        Log.d(TAG,Gson().toJson(body))
        registerDeviceViewModel.registerDevice(body)

    }


    private fun showGPSDisabledAlertToUser() {
        val alertDialogBuilder = android.app.AlertDialog.Builder(this@LoginActivity)
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton(
            "Goto Settings Page To Enable GPS"
        ) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        alertDialogBuilder.setNegativeButton(
            "Cancel"
        ) { dialog, id -> dialog.cancel() }
        alertDialogBuilder.create().show()
    }

    private fun isLocationEnabled(): Boolean {
         var locationManager: LocationManager =
             getSystemService(Context.LOCATION_SERVICE) as LocationManager
         return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
             LocationManager.NETWORK_PROVIDER
         )
        /*val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true
        } else return false*/
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )

        mFusedLocationClient.lastLocation
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    mLocation = task.result
                    latitude = mLocation?.latitude!!
                    longitude = mLocation?.longitude!!
                } else {
                    //requestNewLocationData()
                    Log.w(
                        TAG,
                        "Failed to get location."
                    )
                }
            }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            Log.e("latitude", mLastLocation.latitude.toString())
            Log.e("longitude", mLastLocation.longitude.toString())
            Log.e("accuracy", mLastLocation.accuracy.toString())
            if (mLastLocation != null) {
                //takePicture()
            } else {
                showError(getString(R.string.opps), getString(R.string.location_not_found))
            }
        }
    }
}