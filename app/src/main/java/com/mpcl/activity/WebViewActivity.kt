package com.mpcl.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mpcl.R
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.app.SharedPreference

class WebViewActivity : BaseActivity() {
    private lateinit var webview:WebView
    private lateinit var fbBack:FloatingActionButton
    private lateinit var progressDialog: ProgressBar
    private val baseURL = "https://mobile.maxpacific.org/Magix/WebLogin.htm"
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
        setContentView(R.layout.activity_web_view)
        sharedPreference = SharedPreference(this)
        webview = findViewById(R.id.webview)
        fbBack = findViewById(R.id.fbBack)
        progressDialog = findViewById(R.id.progress)
        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        managePermissions.checkPermissions()
        requestNewLocationData()
        val webSettings: WebSettings = webview.settings
        webSettings.javaScriptEnabled = true


        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (progressDialog.isShown) {
                    progressDialog.visibility = View.GONE
                }
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Toast.makeText(this@WebViewActivity, "Error:$description", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        fbBack.setOnClickListener {
            onBackPressed()
        }


    }

    override fun onPostResume() {
        super.onPostResume()
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
                        if (latitude == 0.0 || longitude == 0.0 || mLocation?.accuracy!! >20000) {
                            requestNewLocationData()
                            if (!isDialogShow()) showDialogForLocation(this)
                        } else {
                            hideDialog()
                            runningThread = false
                            var url = "$baseURL?CID=${sharedPreference.getValueString(Constant.COMPANY_ID)!!}" +
                                    "&BID=${sharedPreference.getValueString(Constant.BID)}&EMPNO=${sharedPreference.getValueString(Constant.EMP_NO)}" +
                                    "&LATITUDE=$latitude&LONGITUDE=$longitude"
                            webview.loadUrl(url)
                        }
                    }else{
                        requestNewLocationData()
                    }

                }
                handlerTask!!.run()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && this.webview.canGoBack()) {
            this.webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    private fun showGPSDisabledAlertToUser() {
        val alertDialogBuilder = android.app.AlertDialog.Builder(this@WebViewActivity)
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
                    Log.w("WebViewActivity", "Failed to get location.")
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
            } else {
                //showError(getString(R.string.opps), getString(R.string.location_not_found))
            }
        }
    }
}