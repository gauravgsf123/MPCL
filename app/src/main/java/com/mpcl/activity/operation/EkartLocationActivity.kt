package com.mpcl.activity.operation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mpcl.R
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.database.EkartDB
import com.mpcl.database.EkartDB_
import com.mpcl.database.ObjectBox
import com.mpcl.databinding.ActivityEkartLocationBinding
import com.mpcl.model.EkartResponseModel
import com.mpcl.model.IntentDataModel
import com.mpcl.util.qr_scanner.QRcodeScanningActivity
import com.mpcl.viewmodel.EKartViewModel.EkartRepository
import com.mpcl.viewmodel.EKartViewModel.EkartViewModel
import com.mpcl.viewmodel.EKartViewModel.EkartViewModelFactory
import io.objectbox.Box
import java.util.*

class EkartLocationActivity : BaseActivity(), TextToSpeech.OnInitListener {

    /*private val textToSpeechEngine: TextToSpeech by lazy {
        TextToSpeech(this,
            TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.ENGLISH
                }
            })
    }*/

    private var tts: TextToSpeech? = null
    private lateinit var binding: ActivityEkartLocationBinding
    private lateinit var managePermissions: ManagePermissions
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_CAMERA_CAPTURE = 1002
    private var intentDataModel: IntentDataModel? = null
    private lateinit var ekartRepository: EkartRepository
    private lateinit var ekartViewModel: EkartViewModel
    private lateinit var ekartViewModelFactory: EkartViewModelFactory
    private var selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT
    private var eKartList: List<EkartResponseModel>? = null
    private val permissionList = listOf(
        Manifest.permission.CAMERA
    )

    private var eKartBox: Box<EkartDB>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEkartLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tts = TextToSpeech(this@EkartLocationActivity, this)
        binding.topBar.ivHome.setOnClickListener {
            onBackPressed()
        }

        eKartBox = ObjectBox.boxStore.boxFor(EkartDB::class.java)
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        ekartRepository = EkartRepository()
        ekartViewModelFactory = EkartViewModelFactory(ekartRepository)
        ekartViewModel =
            ViewModelProvider(this, ekartViewModelFactory).get(EkartViewModel::class.java)
        ekartViewModel.eKartResponse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            eKartList = responseModel
            //Log.d(TAG, Gson().toJson(responseModel))
            createTable(responseModel)
        })
        showDialog()
        ekartViewModel.getEKartList()
        managePermissions.checkPermissions()
        selectedScanningSDK = QRcodeScanningActivity.ScannerSDK.MLKIT

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

        binding.imgBarCode.setOnClickListener(View.OnClickListener {
            startScanning()

        })

        //speakOut("")

        /*intent.getData("result")?.let {
            binding.cardOutput.visibility = View.VISIBLE
            var str = it.drop(13).dropLast(9)

            var ekart = eKartBox?.query()?.equal(EkartDB_.lcode, str)?.build()?.findFirst()
            ekart?.location?.let { it1 -> Log.d(TAG, it1) }
            //routeBox?.query()?.equal(RouteDB_.RouteID, id)?.build()?.findFirst()
            binding.output.setText(ekart?.location)
            speakText(ekart?.location)
        }*/

        speakOut("E-kart Location")
        //speakText("E-kart Location")

    }

    override fun onPostResume() {
        super.onPostResume()
        if (sharedPreference.getValueString("result") != null) {
            var scanValue = sharedPreference.getValueString("result")!!
            binding.etBarCode.setText(scanValue)
            var barcode = scanValue.split("-")
            var barCodeEKart = eKartBox?.query(EkartDB_.lcode.equal(barcode[0]))?.build()?.findFirst()
            if (barCodeEKart?.location?.isNullOrEmpty() == false) {
                if (barCodeEKart?.srno != null) {
                    binding.cardOutput.visibility = View.VISIBLE
                    var text = "${barCodeEKart?.srno}. ${barCodeEKart?.location}"
                    binding.slNo.visibility = View.VISIBLE
                    binding.output.setTextColor(ContextCompat.getColor(this, R.color.black));
                    binding.slNo.setText("${barCodeEKart?.srno}")
                    binding.output.setText(barCodeEKart?.location)
                    speakOut(text)
                } else {
                    binding.slNo.visibility = View.GONE
                    binding.output.setTextColor(ContextCompat.getColor(this, R.color.red));
                    binding.output.text = "Not Readable"
                }
            } else {
                var QRcode = scanValue.split("|")
                QRcode.forEach {
                    if (it.length == 7) {
                        var ekart = eKartBox?.query(EkartDB_.lcode.equal(it))?.build()?.findFirst()
                        ekart?.location?.let { it1 -> Log.d(TAG, it1) }
                        //routeBox?.query()?.equal(RouteDB_.RouteID, id)?.build()?.findFirst()
                        if (ekart?.srno != null) {
                            binding.cardOutput.visibility = View.VISIBLE
                            var text = "${ekart?.srno}. ${ekart?.location}"
                            binding.slNo.visibility = View.VISIBLE
                            binding.output.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.black
                                )
                            );
                            binding.slNo.text = "${ekart?.srno}"
                            binding.output.text = ekart?.location
                            speakOut(text)
                        } else {
                            binding.slNo.visibility = View.GONE
                            binding.output.setTextColor(ContextCompat.getColor(this, R.color.red));
                            binding.output.text = "Not Readable"

                        }
                    }
                }

            }


            sharedPreference.removeValue("result")
        }
    }

    /*fun speakText(location: String?) {
        Log.e(TAG, location.toString())
        val text = binding.output.text.toString().trim()
        if (location?.isNotEmpty()!!) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
            } else {
                textToSpeechEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null)
            }
        } else {
            Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_LONG).show()
        }


    }*/

    fun printDb() {
        for (value in eKartBox?.all!!)
            Log.e(TAG, value?.srno!!)
    }

    private fun createTable(responseModel: List<EkartResponseModel>) {
        val iterator = responseModel.listIterator()
        var _id: MutableList<String> = mutableListOf()

        for (t in responseModel) {
            t.lcode?.let { _id.add(it) }
        }

        /*for (t in routeBox?.all!!) {
            for (id in _id) {
                if(id != t.RouteID) {
                    routeBox?.remove(t)
                }
            }
        }*/
        eKartBox?.removeAll()
        for (data in responseModel) {
            //if(!isExist(data.RouteID)){
            //Log.d(TAG, getEkartData(data).srno.toString())
            eKartBox?.put(getEkartData(data))
        }
        Log.d(TAG, eKartBox?.all?.size.toString())
    }

    fun getEkartData(item: EkartResponseModel): EkartDB {
        val ekartDB = EkartDB()
        ekartDB.srno = item.srno
        ekartDB.lcode = item.lcode
        ekartDB.location = item.location

        return ekartDB
    }


    fun Intent.getData(key: String): String? {
        return extras?.getString(key) ?: null
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

    override fun onPause() {
        //textToSpeechEngine.stop()
        super.onPause()
    }

    private fun speakOut(text: String) {
        //val text = text
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onDestroy() {
        // Shutdown TTS
        //textToSpeechEngine.shutdown()
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts tts.setLanguage(new Locale("hi"));
            val result = tts!!.setLanguage(Locale("hi", "IN"))
            tts!!.setPitch(1.0f) // saw from internet
            tts!!.setSpeechRate(0.8f)

            val voices = tts!!.voices
            for (voice in voices) {
                Log.v(TAG, voice.name)
                if (voice.name == "hi-in-x-cfn#female_2-local") {
                    tts!!.voice = voice
                }
            }
            //tts!!.voice = Voice(Raw)// f denotes float, it actually type casts 0.5 to float
            //tts!!.setLanguage(Locale.US);


            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            } else {
                //buttonSpeak!!.isEnabled = true
                speakOut("E-kart")
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

}