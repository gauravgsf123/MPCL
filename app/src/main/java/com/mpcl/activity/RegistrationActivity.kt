 package com.mpcl.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.biometric.BiometricPrompt
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.mpcl.BuildConfig
import com.mpcl.R
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.Constant.Companion.CIPHERTEXT_WRAPPER
import com.mpcl.app.Constant.Companion.SHARED_PREFS_FILENAME
import com.mpcl.app.ManagePermissions
import com.mpcl.databinding.ActivityRegistrationBinding
import com.mpcl.model.RegistrationResponseModel
import com.mpcl.util.CryptographyManager
import com.mpcl.viewmodel.registrationViewModel.*
import com.mpcl.viewmodel.registrationViewModel.*
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegistrationActivity : BaseActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var registrationRepositoty: RegistrationRepositoty
    private lateinit var registrationViewModelFactory: RegistrationViewModelFactory
    private lateinit var registrationViewModel: RegistrationViewModel

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private  var mLocation : Location?=null
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private var runningThread = true
    private var handler: Handler? = null
    private var handlerTask: Runnable? = null
    private var registrationResponseModel: RegistrationResponseModel?=null
    private var counter = 0
    private lateinit var managePermissions: ManagePermissions
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_CAMERA_CAPTURE = 1002
    private var compressedImage: File? = null
    private var mCurrentPhotoPath: String? = null
    private var path:String?=null
    private val permissionList = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    var mediaPath: String = "/storage/emulated/0/Android/data/com.mpcl/files/Pictures/"
    var attendanceType:Int=1
    private lateinit var biometricPrompt: BiometricPrompt
    private var cryptographyManager = CryptographyManager()
    private val ciphertextWrapper
        get() = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            applicationContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHERTEXT_WRAPPER
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appBar.ivHome.setOnClickListener {
            onBackPressed()
        }

        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        attendanceType = intent.getIntExtra(Constant.ATTENDANCE_TYPE,1)
        binding.companyId.setText(sharedPreference.getValueString(Constant.COMPANY_ID).toString())

        binding.next.setOnClickListener{
            if(attendanceType==1)
                markAttendance()
            else if(attendanceType==2)
                markSalesAttendance()
        }

        binding.selfieImage.setOnClickListener{
            val b = managePermissions.checkPermissions()
            if (b) {
                takePicture()
            }
        }

        binding.empCode.doAfterTextChanged {
            if(it.toString().length==7){
                showDialog()
                val body = mapOf<String,String>("CID" to binding.companyId.text.toString(), "EMPNO" to binding.empCode.text.toString())
                registrationViewModel.registration(body)
            }
            /*registrationViewModel.onLoginDataChanged(
                binding.companyId.text.toString().trim(),
                binding.empCode.text.toString().trim()
            )*/
        }

        registrationRepositoty = RegistrationRepositoty()
        registrationViewModelFactory = RegistrationViewModelFactory(registrationRepositoty)
        registrationViewModel = ViewModelProvider(this,registrationViewModelFactory).get(
            RegistrationViewModel::class.java)

        registrationViewModel.registrationResponsse.observe(this, Observer {
            hideDialog()
            registrationResponseModel = it.get(0) ?: return@Observer
            //it[0].branchName?.let { it1 -> Log.d(TAG, it1) }
            Log.d("respose",Gson().toJson(it))
            if(it.size>0){
                binding.textInputLayoutName.visibility = View.VISIBLE
                binding.textInputLayoutBranch.visibility = View.VISIBLE
                binding.name.setText(it[0].empName)
                binding.branch.setText(it[0].branchName)
                if(attendanceType==1){
                    Log.d("IMEI_NO",getDeviceIMEIId(this)+" : "+ it[0].imeino)
                    /*if(getDeviceIMEIId(this)!= it[0].imeino){
                        showError(getString(R.string.opps),"Device not match.")
                    }else*/ if (it[0].longitue?.let { it1 ->
                            it[0].latitude?.let { it2 ->
                                getLocation(
                                    it2.toDouble(),
                                    it1?.toDouble()
                                )
                            }
                        }?.let { it2 ->
                            calculateDistance(getLocation(latitude, longitude),
                                it2
                            )
                        }!! > 500){
                        //if(calculateDistance(getLocation(latitude, longitude), getLocation(28.611936, 77.290313))>250){
                        //accuracyMeter = calculateDistance(getLocation(latitude, longitude), getLocation(28.53453, 77.78878))
                        showError(getString(R.string.opps),getString(R.string.you_are_not_on_location))
                    }else{
                    binding.selfieImage.visibility = View.VISIBLE
                    }
                }else{
                    binding.selfieImage.visibility = View.VISIBLE
                }

            }else{
                showToast(getString(R.string.something_wrong_please_try_again))
                binding.textInputLayoutName.visibility = View.GONE
                binding.textInputLayoutBranch.visibility = View.GONE
                binding.selfieImage.visibility = View.GONE
            }
        })

        registrationViewModel.loginForm.observe(this, Observer {
            val loginResponseModel = it ?: return@Observer
            when (loginResponseModel) {
                is SuccessfulLoginFormState -> {binding.next.isEnabled = loginResponseModel.isDataValid
                    /*binding.textInputLayoutName.visibility = View.VISIBLE
                    binding.textInputLayoutBranch.visibility = View.VISIBLE*/
                    //binding.next.visibility = View.VISIBLE
                    showDialog()
                    val body = mapOf<String,String>("CID" to binding.companyId.text.toString().trim(), "EMPNO" to binding.empCode.text.toString().trim())
                    Log.d("body",body.toString())
                    registrationViewModel.registration(body)

                }
                is FailedLoginFormState -> {
                    loginResponseModel.usernameError?.let { binding.companyId.error = getString(it) }
                    loginResponseModel.passwordError?.let { binding.empCode.error = getString(it) }
                    binding.textInputLayoutName.visibility = View.GONE
                    binding.textInputLayoutBranch.visibility = View.GONE
                    binding.next.visibility = View.GONE

                }
            }
        })

        registrationViewModel.markAttendanceResponse.observe(this, Observer {
            hideDialog()
            val attendance = it ?: return@Observer
            Log.d("respose",Gson().toJson(attendance))
            if(attendance[0].Response.equals("Success")){
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getString(R.string.success))
                    .setContentText(getString(R.string.your_attendance_done))
                    .setConfirmClickListener { sDialog -> // reuse previous dialog instance
                        sDialog.dismiss()
                        onBackPressed()
                    }
                    .show()
            }
        })


    }

    protected fun getLocation(lat: Double, long: Double): Location {
        val location = Location("")
        location.setLatitude(lat)
        location.setLongitude(long)
        return location
    }

    protected fun calculateDistance(loc1: Location, loc2: Location): Int {
        val distanceInMeters = loc1.distanceTo(loc2)
        Log.d(TAG, "total distance : $distanceInMeters")
        return distanceInMeters.toInt()
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
                        if (latitude == 0.0 || longitude == 0.0 || mLocation?.accuracy!! >16000) {
                            requestNewLocationData()
                            counter++
                            if(counter>60){
                                hideDialog()
                                runningThread = false
                                showError("Error","GPS not found, Please try later.")
                            }else{
                                if (!isDialogShow()) showDialogForLocation(this)
                            }
                            //if (!isDialogShow()) showDialogForLocation(this)
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

    private fun showGPSDisabledAlertToUser() {
        val alertDialogBuilder = AlertDialog.Builder(this@RegistrationActivity)
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton(
            "Goto Settings Page To Enable GPS"
        ) { dialog, id -> startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        alertDialogBuilder.setNegativeButton(
            "Cancel"
        ) { dialog, id -> dialog.cancel() }
        alertDialogBuilder.create().show()
    }

    private fun isLocationEnabled(): Boolean {
        /* var locationManager: LocationManager =
             getSystemService(Context.LOCATION_SERVICE) as LocationManager
         return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
             LocationManager.NETWORK_PROVIDER
         )*/
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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

    private fun markAttendance() {
        showDialog()
        val file = File(path)
        Log.d(TAG,"image_name : ${file.name}")
        val filePart = MultipartBody.Part.createFormData(
            "dataFile",
            file.name,
            RequestBody.create("image/*".toMediaTypeOrNull(), file)
        )
        Log.d(TAG,"COMPANY_ID : ${sharedPreference.getValueString(Constant.COMPANY_ID)}")
        Log.d(TAG,"empCode : ${binding.empCode.text.toString().trim()}")
        Log.d(TAG,"bid : ${registrationResponseModel?.bid}")
        Log.d(TAG,"getDeviceIMEIId : ${getDeviceIMEIId(this)}")
        Log.d(TAG,"MOBILE : ${sharedPreference.getValueString(Constant.MOBILE)}")
        registrationViewModel.markAttendance(
            filePart,
            sharedPreference.getValueString(Constant.COMPANY_ID)?.let { getPart(it) },
            getPart(binding.empCode.text.toString().trim()),
            registrationResponseModel?.bid?.let { getPart(it) },
            getDeviceIMEIId(this)?.let { getPart(it)},
            sharedPreference.getValueString(Constant.MOBILE)?.let { getPart(it) }
        )
    }

    private fun markSalesAttendance() {
        showDialog()
        val file = File(path)
        Log.d(TAG,"image_name : ${file.name}")
        val filePart = MultipartBody.Part.createFormData(
            "dataFile",
            file.name,
            RequestBody.create("image/*".toMediaTypeOrNull(), file)
        )
        registrationViewModel.markSalesAttendance(
            filePart,
            sharedPreference.getValueString(Constant.COMPANY_ID)?.let { getPart(it) },
            getPart(binding.empCode.text.toString().trim()),
            registrationResponseModel?.bid?.let { getPart(it) },
            getDeviceIMEIId(this)?.let { getPart(it)},
            sharedPreference.getValueString(Constant.MOBILE)?.let { getPart(it) }
        )
    }


    private fun takePicture() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()
        if(file!=null){
            Log.e("actual file path", file.path)
            val uri: Uri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }else{
            Log.d("file", "File not created")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if (requestCode == REQUEST_IMAGE_CAPTURE ) {
                if(mCurrentPhotoPath!=null){
                    val auxFile = File(mCurrentPhotoPath)
                    customCompressImage(auxFile)
                }else{
                    showToast("File Empty! Try Again")
                    Log.e("file_path", "File Empty! Try Again");
                }
            }
        }

    }

    private fun customCompressImage(actualImage: File) {
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                compressedImage = Compressor.compress(this@RegistrationActivity, imageFile) {
                    resolution(640, 480)
                    val destination = File(imageFile.parent, imageFile.name)
                    destination(destination)
                    quality(50)
                    format(Bitmap.CompressFormat.JPEG)
                    size(180_152) // 1 MB
                }
                setCompressedImage()
            }
        }
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            var bitmap: Bitmap = BitmapFactory.decodeFile(it.path)
            binding.selfieImage.setImageBitmap(bitmap)
            binding.selfie.visibility = View.GONE
            binding.selfieImage.visibility = View.VISIBLE
            binding.next.visibility = View.VISIBLE


            //val sdcard = Environment.getExternalStorageDirectory()
            /*val timeStamp: String = SimpleDateFormat("dd/MM/yyyy_HH-mm-ss").format(Date())
            val from = File(mediaPath,File(it.absolutePath).name)
            val to = File(mediaPath, "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${binding.empCode.text.toString().trim()}_$timeStamp.jpg")
            from.renameTo(to)*/

            path = bitmap?.let { bitmap -> bitmapToFile(bitmap, it.name) }.toString()
            path = it.absolutePath
            Log.d("final_path", path!!)

        }
    }


    fun bitmapToFile(
        bitmap: Bitmap,
        fileNameToSave: String
    ): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            var filePath =mediaPath+fileNameToSave
            /*file = File(
                Environment.getExternalStorageDirectory().toString() + File.separator +"PS_Scanner"+ fileNameToSave
            )*/
            var file = File(filePath)
            file.createNewFile()
            Log.d(TAG, file.canWrite().toString())
            /*if(file.canWrite()){
                file.createNewFile()
            }*/
            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70 /*ignored for PNG*/, bos); // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()
            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss_").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${binding.empCode.text.toString().trim()}_${timeStamp}", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }




}