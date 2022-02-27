package com.mpcl.app

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*


open abstract class BaseActivity : AppCompatActivity() {
    private lateinit var txtHeading: TextView
    protected lateinit var sharedPreference: SharedPreference
    protected lateinit var toolbar: Toolbar
    protected lateinit var TAG:String

    private lateinit var pDialog: SweetAlertDialog
    //protected lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* binding = ActivityMainBinding.inflate(layoutInflater)
         val view = binding.root
         setContentView(view)*/
        //setContentView(getLayoutResourceId())
        firebaseAnalytics = Firebase.analytics
        sharedPreference = SharedPreference(this)
        TAG = this.javaClass.simpleName
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)


    }

    open fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    protected open fun showError(title: String, message: String){
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText(title)
            .setContentText(message)
            .show()
    }

    protected open fun showDialogForLocation(context: Context){

        pDialog?.let {
            it.progressHelper.barColor = Color.parseColor("#C70B0D")
            it.titleText = "Please wait, Featching your location."
            it.setCancelable(false)
            it.show()
        }
    }
    protected open fun isDialogShow():Boolean{
        var value:Boolean= false
        pDialog?.let { when{ it.isShowing->{value=true}
        }}
        return value
    }

    protected open fun showDialog(){
        pDialog.progressHelper.barColor = Color.parseColor("#C70B0D")
        pDialog.titleText = "Loading ..."
        pDialog.setCancelable(true)
        pDialog.show()
    }
    protected open fun hideDialog(){
        pDialog?.let { when{ it.isShowing->  pDialog.dismiss() }}
    }

    protected open fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    protected open fun setHeading(resId: Int) {
        //if (txtHeading == null) txtHeading = findViewById<TextView>(R.id.txtHeading)
        //if (txtHeading != null) txtHeading.setText(resId)
    }

    protected open fun getDateTime():String{
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        return currentDate;
    }

    fun getDeviceId():String{
        return Settings.Secure.getString(
            this.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    open fun getDeviceIMEIId(context: Context): String? {
        val deviceId: String
        deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } else {
            val mTelephony = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (mTelephony.deviceId != null) {
                mTelephony.deviceId
            } else {
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            }
        }
        return deviceId
    }

    protected open fun getDeviceIMEI(): String? {
        var deviceUniqueIdentifier: String? = null
        val tm = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (null != tm) {
            deviceUniqueIdentifier = tm.deviceId
        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length) {
            deviceUniqueIdentifier =
                Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        }
        return deviceUniqueIdentifier
    }

    fun getPart(filed: String): RequestBody {
        return filed.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
    }


    fun TextView?.getText(): String = this?.text?.toString() ?: ""

    fun TextView?.isEmpty(): Boolean = this?.text?.isEmpty() ?: false


    //protected abstract fun getLayoutResourceId(): ViewBinding
    protected open fun backPress() {
        overridePendingTransition(R.anim.fade_out, R.anim.fade_in)
    }

    protected open fun startNewActivity(activity: Activity) {
        startActivity(Intent(this, activity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }



    companion object{

    }
}