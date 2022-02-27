package com.mpcl.activity.operation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.mpcl.BuildConfig
import com.mpcl.R
import com.mpcl.activity.OptionActivity
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.databinding.ActivityAcCopyUploadBinding
import com.mpcl.model.IntentDataModel
import com.mpcl.util.qr_scanner.BarcodeScanningActivity
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


class AcCopyUploadActivity : BaseActivity(),View.OnClickListener {
    private lateinit var managePermissions: ManagePermissions
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_CAMERA_CAPTURE = 1002
    private var compressedImage: File? = null
    private var mCurrentPhotoPath: String? = null
    private val permissionsRequestCode = 123
    private var intentDataModel: IntentDataModel?=null
    private var path:String?=null
    private var selectedScanningSDK = BarcodeScanningActivity.ScannerSDK.MLKIT
    private val permissionList = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var mediaPath: String = "/storage/emulated/0/Android/data/com.mpcl/files/Pictures/"
    private lateinit var barCodeViewModel: BarCodeViewModel
    private lateinit var barCodeRepository: BarCodeRepository
    private lateinit var barCodeViewModelFactory: BarCodeViewModelFactory
    private lateinit var binding: ActivityAcCopyUploadBinding
    private var intentType:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcCopyUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar!!.title = getString(R.string.ac_copy_upload)
        actionbar.setDisplayHomeAsUpEnabled(true)*/

        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        binding.imgBarCode.setOnClickListener(this)
        binding.btnPhoto.setOnClickListener(this)
        binding.btnUpdate.setOnClickListener(this)

        intentDataModel = intent.getParcelableExtra(Constant.INTENT_TYPE)
        Log.d(TAG, intentDataModel?.type.toString() + " : " + intentDataModel?.value)
        if(intentDataModel?.value!=null){
            binding.barCode.setText(intentDataModel?.value)
        }
        if(intentDataModel?.type!=0){
            if(intentDataModel?.type==1)
            binding.title.text = "A/C Copy Upload"
            else binding.title.text = "Pod Upload"
        }
       /* intent.getData("result")?.let{
                Log.d(TAG, it)
            binding.barCode.setText(it)
            }*/

        barCodeRepository =  BarCodeRepository()
        barCodeViewModelFactory = BarCodeViewModelFactory(barCodeRepository)
        barCodeViewModel = ViewModelProvider(this, barCodeViewModelFactory).get(BarCodeViewModel::class.java)
        barCodeViewModel.responseBarCode.observe(this, androidx.lifecycle.Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if (responseModel.size > 0) {
                Log.d(TAG, responseModel.get(0).Response.toString())
                if (responseModel.get(0).Response.toString().equals("Success")) {
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(getString(R.string.success))
                        .setContentText(getString(R.string.congrats_data_successful_uploaded))
                        .setConfirmClickListener(object : SweetAlertDialog.OnSweetClickListener {
                            override fun onClick(sDialog: SweetAlertDialog) {
                                // reuse previous dialog instance
                                //onBackPressed()
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
    }



    fun Intent.getData(key: String): String? {
        return extras?.getString(key) ?: null
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBarCode -> {
                selectedScanningSDK = BarcodeScanningActivity.ScannerSDK.MLKIT
                startScanning()
            }
            R.id.btnPhoto -> {
                val b = managePermissions.checkPermissions()
                if (b) {
                    if (intentDataModel?.value != null) {
                        takePicture()
                    } else {
                        showError(getString(R.string.opps), "please scan code first")
                    }

                }
            }
            R.id.btnUpdate -> validateForm()
        }
    }


    private fun validateForm() {
        when{
            binding.barCode.text?.isNotBlank()==false->{ showError(
                getString(R.string.opps),
                "please scan you code"
            )}
            path==null->{showError(
                getString(R.string.opps),
                "Please take photo"
            )}
            else -> updateLocation()

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
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                compressedImage = Compressor.compress(this@AcCopyUploadActivity, imageFile) {
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
            "${sharedPreference.getValueString(Constant.COMPANY_ID)}_${intentDataModel?.value}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun openCameraWithScanner() {
        intentDataModel?.let { BarcodeScanningActivity.start(this, selectedScanningSDK, it) }
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

    protected fun updateLocation(){


        if(intentDataModel?.type!=0){

            if(intentDataModel?.type==1){
                showDialog()
                val file = File(path)
                Log.d(TAG,"image_name : ${file.name}")
                val filePart = MultipartBody.Part.createFormData(
                    "dataFile",
                    file.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), file)
                )
                barCodeViewModel.uploadAcCopyData(
                    filePart,
                    sharedPreference.getValueString(Constant.COMPANY_ID)?.let { getPart(it) },
                    sharedPreference.getValueString(Constant.EMP_NO)?.let { getPart(it) },
                    sharedPreference.getValueString(Constant.MOBILE)?.let { getPart(it) },
                    getPart(binding.barCode.text.toString().trim()),
                    getDeviceId()?.let { getPart(it) }
                )
            }else if(intentDataModel?.type==2){
                showDialog()
                val file = File(path)
                val filePart = MultipartBody.Part.createFormData(
                    "dataFile",
                    file.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), file)
                )
                barCodeViewModel.uploadPODCopyData(
                    filePart,
                    sharedPreference.getValueString(Constant.COMPANY_ID)?.let { getPart(it) },
                    sharedPreference.getValueString(Constant.EMP_NO)?.let { getPart(it) },
                    sharedPreference.getValueString(Constant.MOBILE)?.let { getPart(it) },
                    getPart(binding.barCode.text.toString().trim()),
                    getDeviceId()?.let { getPart(it) }
                )
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, OptionActivity::class.java))
        finish()
    }
}