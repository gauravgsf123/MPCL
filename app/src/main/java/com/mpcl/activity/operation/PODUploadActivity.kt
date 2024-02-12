package com.mpcl.activity.operation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.mpcl.BuildConfig
import com.mpcl.R
import com.mpcl.activity.pickup.PODDelayReasonResponse
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.databinding.ActivityPodUploadBinding
import com.mpcl.model.IntentDataModel
import com.mpcl.model.PodDateLimitResponse
import com.mpcl.util.RealPathUtil
import com.mpcl.util.Utils
import com.mpcl.util.qr_scanner.QRcodeScanningActivity
import com.mpcl.viewmodel.barCodeViewModel.BarCodeRepository
import com.mpcl.viewmodel.barCodeViewModel.BarCodeViewModel
import com.mpcl.viewmodel.barCodeViewModel.BarCodeViewModelFactory
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PODUploadActivity : BaseActivity(), View.OnClickListener {
    private lateinit var podDelayReasonResponse: PODDelayReasonResponse
    private var pickupType:String = ""
    private var deliveryType:String = ""
    private var reason:String = ""
    private var rejectReason:String=""
    private lateinit var requestNo:String

    private lateinit var managePermissions: ManagePermissions
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_CAMERA_CAPTURE = 1002
    private val REQUEST_GALLERY_CAPTURE = 1003
    private var compressedImage: File? = null
    private var mCurrentPhotoPath: String? = null
    private val permissionsRequestCode = 123
    private var intentDataModel: IntentDataModel?=null
    private var path:String?=null
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )
    var mediaPath: String = "/storage/emulated/0/Android/data/com.mpcl/files/Pictures/"
    private lateinit var barCodeViewModel: BarCodeViewModel
    private lateinit var barCodeRepository: BarCodeRepository
    private lateinit var barCodeViewModelFactory: BarCodeViewModelFactory
    private var minDate = Utils.getDate("dd/MM/yyyy")
    private lateinit var binding: ActivityPodUploadBinding
    private var isDelay = false
    private lateinit var podDateLimitResponse: PodDateLimitResponse

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPodUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }
        barCodeRepository =  BarCodeRepository()
        barCodeViewModelFactory = BarCodeViewModelFactory(barCodeRepository)
        barCodeViewModel = ViewModelProvider(this, barCodeViewModelFactory).get(BarCodeViewModel::class.java)

        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        val pickupStatusOption = resources.getStringArray(R.array.pod_status_type)
        val pickupStatusOptionAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, pickupStatusOption)
        binding.type.setAdapter(pickupStatusOptionAdapter)

        val deliveryTypeOption = resources.getStringArray(R.array.delivery_type_option)
        val deliveryTypeOptionAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, deliveryTypeOption)
        binding.deliveryType.setAdapter(deliveryTypeOptionAdapter)


        var body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!
        )
        //pickupViewModel.pickupTypeReason(body)
        barCodeViewModel.delayReason(body)
        showDialog()

        binding.type.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                rejectReason =""
                deliveryType = ""
                binding.cNoteNumber.setText("")
                binding.deliveryType.setText("",false)
                binding.reason.setText("",false)

                when(parent.getItemAtPosition(position)){
                    "Delivered"->{
                        binding.cNoteNumber.setText("")
                        binding.groupUndelivered.visibility = View.GONE
                        binding.groupRTO.visibility = View.GONE
                        binding.groupImage.visibility = View.VISIBLE
                        pickupType = parent.getItemAtPosition(position) as String

                        binding.tvCalender.text = podDateLimitResponse.DrsDate!!

                        if(!isDelay){
                            binding.groupDeliveredDelay.visibility = View.GONE
                            binding.groupDelivered.visibility = View.VISIBLE
                        }else{
                            binding.groupDelivered.visibility = View.GONE
                            binding.groupDeliveredDelay.visibility = View.VISIBLE
                            binding.deliveryType.setText(resources.getString(R.string.internal),false)
                            deliveryType = resources.getString(R.string.internal)
                            binding.reason.setText("",false)
                            val pickupNotDoneReasonAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, podDelayReasonResponse.DELINTERNAL)
                            binding.reason.setAdapter(pickupNotDoneReasonAdapter)
                        }

                    }
                    "Undelivered"->{
                        binding.cNoteNumber.setText("")
                        binding.groupDelivered.visibility = View.GONE
                        binding.groupRTO.visibility = View.GONE
                        binding.groupImage.visibility = View.GONE
                        binding.groupUndelivered.visibility = View.VISIBLE
                        pickupType = parent.getItemAtPosition(position) as String
                        binding.deliveryType.setText(resources.getString(R.string.internal),false)
                        deliveryType = resources.getString(R.string.internal)
                        binding.reason.setText("",false)
                        val pickupNotDoneReasonAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, podDelayReasonResponse.UNDINTERNAL)
                        binding.reason.setAdapter(pickupNotDoneReasonAdapter)
                        rejectReason =""
                    }
                    "R.T.O"->{
                        binding.groupDelivered.visibility = View.GONE
                        binding.groupUndelivered.visibility = View.GONE
                        binding.groupImage.visibility = View.GONE
                        binding.groupRTO.visibility = View.VISIBLE
                        deliveryType = ""
                        pickupType = parent.getItemAtPosition(position) as String
                        rejectReason =""
                    }
                    else -> pickupType = ""
                }
            }

        binding.deliveryType.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                when(parent.getItemAtPosition(position)) {
                    resources.getString(R.string.internal)->{
                        deliveryType = parent.getItemAtPosition(position) as String
                        when(pickupType){
                            "Delivered"->{
                                val pickupNotDoneReasonAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, podDelayReasonResponse.DELINTERNAL)
                                binding.reason.setAdapter(pickupNotDoneReasonAdapter)
                            }
                            "Undelivered"->{
                                val pickupNotDoneReasonAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, podDelayReasonResponse.UNDINTERNAL)
                                binding.reason.setAdapter(pickupNotDoneReasonAdapter)
                            }
                        }
                    }
                    resources.getString(R.string.external)->{
                        deliveryType = parent.getItemAtPosition(position) as String
                        when(pickupType){
                            "Delivered"->{
                                val pickupNotDoneReasonAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, podDelayReasonResponse.DELEXTERNAL)
                                binding.reason.setAdapter(pickupNotDoneReasonAdapter)
                            }
                            "Undelivered"->{
                                val pickupNotDoneReasonAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, podDelayReasonResponse.UNDEXTERNAL)
                                binding.reason.setAdapter(pickupNotDoneReasonAdapter)
                            }
                        }
                    }
                }
            }

        binding.reason.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                rejectReason = (parent.getItemAtPosition(position).toString())
            }
        binding.imgBarCode.setOnClickListener(this)
        binding.ivCamera.setOnClickListener(this)
        binding.ivGallery.setOnClickListener(this)
        binding.save.setOnClickListener(this)
        binding.tvCalender.setOnClickListener(this)

        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }

        binding.etBarCode.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            val inputMethod: InputMethodManager =
                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (inputMethod != null) {
                inputMethod.hideSoftInputFromWindow(v.windowToken, 0)
            }
            true
        }

        binding.etBarCode.setOnFocusChangeListener {
                view, b ->
            val inputMethod: InputMethodManager =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if(b) inputMethod.hideSoftInputFromWindow(view.windowToken, 0)
        }

        setObserver()
    }

    override fun onPostResume() {
        super.onPostResume()

        if(sharedPreference.getValueString("result")?.isNotEmpty() == true){
            var str = sharedPreference.getValueString("result")
            binding.etBarCode.setText(str)
            Log.d("etBarCode","$str")
            getLimitDate(str!!.trim())
            sharedPreference.removeValue("result")
        }
    }

    /*private fun setOnserver() {
        barCodeViewModel.responseBarCode.observe(this, androidx.lifecycle.Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                Log.d(TAG, responseModel[0].Response.toString())
                if (responseModel[0].Response.toString() == "Success") {
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(getString(R.string.success))
                        .setContentText(getString(R.string.congrats_data_successful_uploaded))
                        .setConfirmClickListener(object : SweetAlertDialog.OnSweetClickListener {
                            override fun onClick(sDialog: SweetAlertDialog) {
                                sDialog.dismiss()
                                binding.imgSelfi.setImageResource(0);
                                binding.imgSelfi.setBackgroundResource(R.drawable.ic_image_placeholder)
                                path = null
                                binding.barCode.setText("")
                            }
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
    }*/
    private fun setObserver() {
        /*pickupViewModel.pickupReasonResponseModel.observe(this, androidx.lifecycle.Observer {
            if(it.isNotEmpty()){
                hideDialog()
                var arr = ArrayList<String>()
                it.forEach {str->
                    arr.add(str.Types!!)

                }

            }
        })

        pickupViewModel.savePickupResponse.observe(this, androidx.lifecycle.Observer {
            hideDialog()
            //if(it.isNotEmpty() && it[0].equals("Success")) {
            if (it[0].Response.toString() == "Success") {
                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getString(R.string.success))
                    .setContentText(getString(R.string.congrats_data_successful_uploaded))
                    .setConfirmClickListener { sDialog -> sDialog.dismiss() }
                    .show()
            }
            //}
        })*/
        barCodeViewModel.responseLimitDate.observe(this, androidx.lifecycle.Observer {
            hideDialog()
            var response = it ?:return@Observer
            reset()
            if(response.isNotEmpty()){
                podDateLimitResponse = response[0]
                isDelay = Utils.checkDate(response[0].EdDate!!,response[0].DrsDate!!)
                binding.tvCalender.text = response[0].DrsDate!!
                binding.type.setText(resources.getString(R.string.delivered),false)
                pickupType = resources.getString(R.string.delivered)
                if(podDateLimitResponse.Response=="Failed"){
                    binding.etBarCode.setText("")
                    binding.textInputLayoutType.visibility = View.GONE
                    binding.save.isEnabled = false
                    showError(
                        getString(R.string.opps),
                        podDateLimitResponse.Message!!
                    )
                }else {
                    binding.save.isEnabled = true
                    binding.groupImage.visibility = View.VISIBLE
                    binding.textInputLayoutType.visibility = View.VISIBLE
                    binding.tvCalender.visibility = if(response.isEmpty())View.INVISIBLE else View.VISIBLE
                    if (!isDelay) {
                        binding.groupDeliveredDelay.visibility = View.GONE
                        binding.groupDelivered.visibility = View.VISIBLE
                        deliveryType = ""
                    } else {
                        binding.groupDelivered.visibility = View.GONE
                        binding.groupDeliveredDelay.visibility = View.VISIBLE
                        binding.deliveryType.setText(resources.getString(R.string.internal), false)
                        deliveryType = resources.getString(R.string.internal)
                        binding.reason.setText("", false)
                        val pickupNotDoneReasonAdapter = ArrayAdapter(
                            this,
                            R.layout.drop_down_list_item,
                            podDelayReasonResponse.DELINTERNAL
                        )
                        binding.reason.setAdapter(pickupNotDoneReasonAdapter)
                    }
                }

                /*if(!response[0].DrsDate.toString().isNullOrEmpty())
                    minDate = response[0].DrsDate.toString()
                binding.tvCalender.text = minDate*/
            }

        })

        barCodeViewModel.podDelayReasonResponse.observe(this, androidx.lifecycle.Observer {
            hideDialog()
            var response = it ?: return@Observer
            podDelayReasonResponse = response[0]
        })

        barCodeViewModel.responseBarCode.observe(this, androidx.lifecycle.Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.isNotEmpty()) {
                Log.d(TAG, responseModel[0].Response.toString())
                if (responseModel[0].Response.toString() == "Success") {
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(getString(R.string.success))
                        .setContentText(getString(R.string.congrats_data_successful_uploaded))
                        .setConfirmClickListener(object : SweetAlertDialog.OnSweetClickListener {
                            override fun onClick(sDialog: SweetAlertDialog) {
                                sDialog.dismiss()
                                binding.imgSelfi.setImageResource(0);
                                binding.imgSelfi.setBackgroundResource(R.drawable.ic_image_placeholder)
                                path = null
                                binding.etBarCode.setText("")
                                binding.cNoteNumber.setText("")
                                reset()
                            }
                        })
                        .show()
                }else{
                    showError(
                        getString(R.string.opps),
                        "Failed"
                    )
                }
            } else {
                showError(
                    getString(R.string.opps),
                    "Some thing wrong"
                )
            }


        })
    }

    private fun reset(){
        binding.groupDelivered.visibility = View.GONE
        binding.groupRTO.visibility = View.GONE
        binding.groupUndelivered.visibility = View.GONE
        binding.groupImage.visibility = View.GONE
        binding.textInputLayoutType.visibility = View.GONE
        binding.save.isEnabled = false
        binding.tvCalender.text = ""
        binding.cNoteNumber.setText("")
        binding.deliveryType.setText("",false)
        binding.reason.setText("",false)
        binding.type.setText(resources.getString(R.string.delivered),false)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.imgBarCode -> {
                selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
                startScanning()
            }
            R.id.ivCamera -> {
                val b = managePermissions.checkPermissions()
                if (b) {
                    takePicture()
                    /*if (intentDataModel?.value != null) {
                        takePicture()
                    } else {
                        showError(getString(R.string.opps), "please scan code first")
                    }*/

                }
            }
            R.id.ivGallery->{
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, REQUEST_GALLERY_CAPTURE)
                }
            }
            R.id.tvCalender->showCalender()
            R.id.save->{
                validateForm()
                /*var cNote = binding.etBarCode.text.toString()
                when(pickupType){
                    "Done"->rejectReason=""
                    "Not"->cNote=""
                }
                showDialog()
                val jsonObject = JSONObject()
                jsonObject.put("Response","Success")
                jsonObject.put("RequestNo",requestNo)
                jsonObject.put("etBarCode",cNote)
                //jsonObject.put("ContactName",binding.contactName.text.toString())
                //jsonObject.put("ContactNo",binding.ContactNo.text.toString())
                jsonObject.put("Reason",rejectReason)
                jsonObject.put("Status",pickupType)

                Log.d("JSON",jsonObject.toString())
                var body = mapOf<String, String>(
                    "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
                    "BID" to sharedPreference.getValueString(Constant.BID)!!,
                    "DATASTR" to jsonObject.toString()
                )
                pickupViewModel.pickupSave(body)*/
            }
        }
    }

    private fun getLimitDate(docNumber: String) {
        val mScanDocDataBody = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(Constant.COMPANY_ID)!!,
            "BID" to sharedPreference.getValueString(Constant.BID)!!,
            "DOCNUMBER" to docNumber
        )
        showDialog()
        Log.d(TAG,mScanDocDataBody.toString())
        barCodeViewModel.getLimitDate(mScanDocDataBody)
    }


    private fun showCalender(){
        var mcurrentDate = Calendar.getInstance()
        val mYear: Int = mcurrentDate.get(Calendar.YEAR)
        val mMonth: Int = mcurrentDate.get(Calendar.MONTH)
        val mDay: Int = mcurrentDate.get(Calendar.DAY_OF_MONTH)
        var defaultDate = podDateLimitResponse.DrsDate!!.toString().split(Regex("/"))
        var dd = defaultDate[0].toInt()
        var mm = defaultDate[1].toInt()
        var yy = defaultDate[2].toInt()
        val mDatePicker = DatePickerDialog(
            this@PODUploadActivity,
            { datepicker, selectedyear, selectedmonth, selectedday ->
                mcurrentDate.set(Calendar.YEAR, selectedyear)
                mcurrentDate.set(Calendar.MONTH, selectedmonth)
                mcurrentDate.set(
                    Calendar.DAY_OF_MONTH,
                    selectedday
                )
                val sdf = SimpleDateFormat(
                    resources.getString(
                        R.string.date_card_formate
                    ),
                    Locale.US
                )
                binding.tvCalender.text = sdf.format(
                    mcurrentDate.time
                )
                isDelay = Utils.checkDate(podDateLimitResponse.EdDate!!,binding.tvCalender.text.toString())
                if(!isDelay){
                    binding.groupDeliveredDelay.visibility = View.GONE
                    binding.groupDelivered.visibility = View.VISIBLE
                }else{
                    binding.groupDelivered.visibility = View.GONE
                    binding.groupDeliveredDelay.visibility = View.VISIBLE
                    binding.deliveryType.setText(resources.getString(R.string.internal),false)
                    deliveryType = resources.getString(R.string.internal)
                    binding.reason.setText("",false)
                    val pickupNotDoneReasonAdapter = ArrayAdapter(this, R.layout.drop_down_list_item, podDelayReasonResponse.DELINTERNAL)
                    binding.reason.setAdapter(pickupNotDoneReasonAdapter)
                }

            }, mYear, mMonth, mDay
        )
        //mDatePicker.updateDate(Utils.getCurrentDate(""))
        mDatePicker.setTitle(
            resources.getString(
                R.string.alert_date_select
            )
        )

        mDatePicker.datePicker.maxDate = System.currentTimeMillis()
        if(podDateLimitResponse.Response!="Failed") {
            mDatePicker.datePicker.minDate = Utils.milliseconds(podDateLimitResponse.DrsDate!!)
            mDatePicker.updateDate(yy, mm-1, dd)
        }
        mDatePicker.show()
    }

    private fun validateForm() {
        when{
            binding.etBarCode.text?.isNotBlank()==false->{ showError(
                getString(R.string.opps),
                "Please Scan Code"
            )}
            pickupType.isNullOrEmpty()->{
                showError(
                    getString(R.string.opps),
                    "Please Select Pickup Type"
                )
            }
            pickupType==resources.getString(R.string.delivered)->{
                if(isDelay && rejectReason.isNullOrEmpty()){
                    showError(
                        getString(R.string.opps),
                        "Please Select Delay Reason"
                    )
                }else if(path==null){showError(
                        getString(R.string.opps),
                        "Please Take Photo"
                    )
                }else updateLocation()
            }
            pickupType==resources.getString(R.string.undelivered)->{
                if(rejectReason.isNullOrEmpty()){
                    showError(
                        getString(R.string.opps),
                        "Please Select Delay Reason"
                    )
                }else updateLocation()
            }
            pickupType==resources.getString(R.string.rto)->{
                if(binding.cNoteNumber.text?.isNullOrEmpty()==true){ showError(
                    getString(R.string.opps),
                    "Please Enter C-Note Number")
                }else updateLocation()
            }
        }
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
                    // var txt = String.format("Size : %s", getReadableFileSize(auxFile.length()))
                    //Log.e("actual file size", txt)
                    customCompressImage(auxFile)
                }else{
                    showToast("File Empty! Try Again")
                    Log.e("file_path", "File Empty! Try Again");
                }


//            compressImage(auxFile)

            }
            else if(requestCode == REQUEST_GALLERY_CAPTURE){
                val extras = data!!.data

                Log.d("file_path","${RealPathUtil.getRealPath(this,extras)}")
                val auxFile = File(RealPathUtil.getRealPath(this,extras))
                // var txt = String.format("Size : %s", getReadableFileSize(auxFile.length()))
                //Log.e("actual file size", txt)
                //customCompressImage(auxFile)
                binding.imgSelfi.setImageURI(extras )
                val from = File(mediaPath,File(auxFile.absolutePath).name)
                val to = File(mediaPath, "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${intentDataModel?.value}.jpg")
                from.renameTo(to)
                path = to.absolutePath



                //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, extras)
                //Log.d("file_path","${extras?.toString()} ${extras?.path} $bitmap")
                //binding.imgSelfi.setImageBitmap(bitmap)
                /*extras?.let {
                    File(getRealPathFromURI(this,extras))
                }?.let { customCompressImage(it) }*/
            }
            else if (requestCode == REQUEST_CAMERA_CAPTURE) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openCameraWithScanner()
                }
            }
        }

    }


    private fun customCompressImage(actualImage: File) {
        actualImage.let { imageFile ->
            lifecycleScope.launch {
                compressedImage = Compressor.compress(this@PODUploadActivity, imageFile) {
                    resolution(640, 480)
                    val destination = File(imageFile.parent, imageFile.name.toLowerCase())
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
            binding.imgSelfi.setImageBitmap(bitmap)

            //val sdcard = Environment.getExternalStorageDirectory()
            val from = File(mediaPath,File(it.absolutePath).name)
            val to = File(mediaPath, "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${intentDataModel?.value}.jpg")
            from.renameTo(to)
            path = to.absolutePath
            Log.d("final_path", path!!)

        }
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        // Create an image file name
        Log.d(
            "temp_file",
            "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${intentDataModel?.value}_"
        )
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${intentDataModel?.value}", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
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

    private fun updateLocation(){


        if(intentDataModel?.type!=0){
                if(pickupType==resources.getString(R.string.delivered) && !isDelay) deliveryType =""
                showDialog()
                val file = path?.let {
                    File(it)
                }
            var filePart:MultipartBody.Part?=null
            filePart = if(path!=null) {
                MultipartBody.Part.createFormData(
                    "dataFile",
                    file?.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), file!!)
                )
            }else{
                MultipartBody.Part.createFormData(
                    "dataFile",
                    "",
                    RequestBody.create("text/plain".toMediaTypeOrNull(), "")
                )
            }
                barCodeViewModel.uploadPODCopyData(
                    filePart,
                    sharedPreference.getValueString(Constant.COMPANY_ID)?.let { getPart(it) },
                    sharedPreference.getValueString(Constant.EMP_NO)?.let { getPart(it) },
                    sharedPreference.getValueString(Constant.MOBILE)?.let { getPart(it) },
                    sharedPreference.getValueString(Constant.BID)?.let { getPart(it) },
                    getPart(binding.etBarCode.text.toString().trim()),
                    getPart(binding.tvCalender.text.toString().trim()),
                    getDeviceId()?.let { getPart(it) },
                    getPart(pickupType),
                    getPart(deliveryType),
                    getPart(rejectReason),
                    getPart(binding.cNoteNumber.text.toString().trim()),
                )
        }

    }

    /*override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, OptionActivity::class.java))
        finish()
    }*/
}