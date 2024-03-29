package com.mpcl.util.qr_scanner

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.observe

import com.google.common.util.concurrent.ListenableFuture
import com.mpcl.R
import com.mpcl.activity.operation.AcCopyUploadActivity
import com.mpcl.activity.operation.PODUploadActivity
import com.mpcl.app.Constant
import com.mpcl.databinding.ActivityBarcodeScanningBinding
import com.mpcl.listner.ScanningResultListener
import com.mpcl.model.IntentDataModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val ARG_SCANNING_SDK = "scanning_SDK"

class BarcodeScanningActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun start(context: Context, scannerSDK: ScannerSDK, intentDataModel: IntentDataModel) {
            val starter = Intent(context, BarcodeScanningActivity::class.java).apply {
                putExtra(ARG_SCANNING_SDK, scannerSDK)
                putExtra(Constant.INTENT_TYPE, intentDataModel)
            }
            context.startActivity(starter)
        }
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var binding: ActivityBarcodeScanningBinding
    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private var flashEnabled = false
    private lateinit var intentDataModel: IntentDataModel
    private var scannerSDK: ScannerSDK = ScannerSDK.MLKIT //default is MLKit
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scannerSDK = intent?.getSerializableExtra(ARG_SCANNING_SDK) as ScannerSDK
        intentDataModel = intent.getParcelableExtra<IntentDataModel>(Constant.INTENT_TYPE)!!
        Log.d("intent_data",intentDataModel.type.toString()+" : "+intentDataModel.value)
        /*when (scannerSDK) {
            ScannerSDK.MLKIT -> binding.ivScannerLogo.setImageResource(R.drawable.mlkit_icon)
            ScannerSDK.ZXING -> binding.ivScannerLogo.setImageResource(R.drawable.zxing)
        }*/

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraProviderFuture.addListener(kotlinx.coroutines.Runnable{
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
        /*cameraProviderFuture.addListener(Runnable { val cameraProvider = cameraProviderFuture.get()
                                                  bindPreview(cameraProvider)},ContextCompat.getMainExecutor(this) )*/

        binding.overlay.post {
            binding.overlay.setViewFinder()
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {

        if (isDestroyed || isFinishing) {
            //This check is to avoid an exception when trying to re-bind use cases but user closes the activity.
            //java.lang.IllegalArgumentException: Trying to create use case mediator with destroyed lifecycle.
            return
        }

        cameraProvider?.unbindAll()

        val preview: Preview = Preview.Builder()
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val orientationEventListener = object : OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation : Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageAnalysis.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

        //switch the analyzers here, i.e. MLKitBarcodeAnalyzer, ZXingBarcodeAnalyzer
        class ScanningListener : ScanningResultListener {
            override fun onScanned(result: String) {
                runOnUiThread {
                    imageAnalysis.clearAnalyzer()
                    cameraProvider?.unbindAll()
                    Log.d("result",result)
                    //Toast.makeText(this@BarcodeScanningActivity,result,Toast.LENGTH_LONG).show()
                    intentDataModel.value = result
                    var intent :Intent?=null
                    if(intentDataModel.type==1){
                        intent = Intent(this@BarcodeScanningActivity, AcCopyUploadActivity::class.java)
                    }else intent = Intent(this@BarcodeScanningActivity, PODUploadActivity::class.java)

                    intent.putExtra(Constant.INTENT_TYPE,intentDataModel)
                    startActivity(intent)
                    finish()
                   /* ScannerResultDialog
                        .newInstance(result, object : ScannerResultDialog.DialogDismissListener {
                            override fun onDismiss() {
                                bindPreview(cameraProvider)
                            }
                        })
                        .show(supportFragmentManager, ScannerResultDialog::class.java.simpleName)*/
                }
            }
        }

        var analyzer: ImageAnalysis.Analyzer = MLKitBarcodeAnalyzer(ScanningListener())

        if (scannerSDK == ScannerSDK.ZXING) {
            analyzer = ZXingBarcodeAnalyzer(ScanningListener())
        }

        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

        val camera =
            cameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)

        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            binding.ivFlashControl.visibility = View.VISIBLE

            binding.ivFlashControl.setOnClickListener {
                camera.cameraControl.enableTorch(!flashEnabled)
            }

            camera.cameraInfo.torchState.observe(this) {
                it?.let { torchState ->
                    if (torchState == TorchState.ON) {
                        flashEnabled = true
                        binding.ivFlashControl.setImageResource(R.drawable.ic_round_flash_on)
                    } else {
                        flashEnabled = false
                        binding.ivFlashControl.setImageResource(R.drawable.ic_round_flash_off)
                    }
                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        // Shut down our background executor
        cameraExecutor.shutdown()
    }


    enum class ScannerSDK {
        MLKIT,
        ZXING
    }
}