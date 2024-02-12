package com.mpcl.activity

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.ExpandableListView.OnGroupClickListener
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.view.GravityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.mpcl.R
import com.mpcl.activity.barcode_setting.DeviceSetupActivity
import com.mpcl.activity.barcode_setting.PickupScanActivity
import com.mpcl.activity.barcode_setting.StickerPrintActivity
import com.mpcl.activity.finder.FinderActivity
import com.mpcl.activity.operation.*
import com.mpcl.activity.operation.box_wise_scan.BoxWiseScanActivity
import com.mpcl.activity.operation.boxpacking.BoxPackingActivity
import com.mpcl.activity.pickup.PickupActivity
import com.mpcl.activity.pincode_finder.PincodeFinderActivity
import com.mpcl.activity.todo.TodayActivity
import com.mpcl.activity.todo.TodoActivity
import com.mpcl.adapter.ExpandableListAdapter
import com.mpcl.app.BaseActivity
import com.mpcl.app.Constant
import com.mpcl.app.ManagePermissions
import com.mpcl.custom.BoldTextView
import com.mpcl.custom.RegularButton
import com.mpcl.custom.RegularTextView
import com.mpcl.databinding.ActivityOptionBinding
import com.mpcl.databinding.RegistrationDialogBinding
import com.mpcl.model.IntentDataModel
import com.mpcl.model.MenuModel
import com.mpcl.model.RegistrationResponseModel
import com.mpcl.receiver.AlertReceiver
import com.mpcl.receiver.ConnectivityReceiver
import com.mpcl.util.BiometricPromptUtils
import com.mpcl.util.CryptographyManager
import com.mpcl.viewmodel.registrationViewModel.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class OptionActivity : BaseActivity(),View.OnClickListener, ConnectivityReceiver.ConnectivityReceiverListener {
    private lateinit var alarmManager: AlarmManager
    private lateinit var registaionResponseModel: RegistrationResponseModel
    private lateinit var registrationRepositoty: RegistrationRepositoty
    private lateinit var registrationViewModelFactory: RegistrationViewModelFactory
    private lateinit var registrationViewModel: RegistrationViewModel
    //private lateinit var intentDataModel: IntentDataModel
    lateinit var dialogOption: AlertDialog
    var dialogNoInternet: AlertDialog?=null
    lateinit var dialogRegitration: AlertDialog
    private lateinit var binding: ActivityOptionBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private var cryptographyManager = CryptographyManager()
    private val ciphertextWrapper
        get() = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            applicationContext,
            Constant.SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            Constant.CIPHERTEXT_WRAPPER
        )
    private lateinit var managePermissions : ManagePermissions
    private val permissionList = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE
    )

    private var headerList :MutableList<MenuModel> = mutableListOf()
    private var childList :MutableMap<MenuModel, MutableList<MenuModel>> = hashMapOf()
    var c: Date? = null
    var df: SimpleDateFormat? = null
    //var headerList: List<MenuModel> = ArrayList()
    //var childList = HashMap<MenuModel, List<MenuModel>>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registrationRepositoty = RegistrationRepositoty()
        registrationViewModelFactory = RegistrationViewModelFactory(registrationRepositoty)
        registrationViewModel = ViewModelProvider(this, registrationViewModelFactory).get(
            RegistrationViewModel::class.java
        )

        managePermissions = ManagePermissions(this, permissionList, Constant.REQUEST_PERMISION)
        binding.cvWebview.setOnClickListener(this)
        binding.cvPickup.setOnClickListener(this)
        binding.cvTodo.setOnClickListener(this)
        binding.cvPincodeFinder.setOnClickListener(this)
        binding.cvTracking.setOnClickListener(this)
        registrationViewModel.employeeVerificationResponsse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer
            if(responseModel.isNotEmpty()){
                showPopupMessage(getString(R.string.success),getString(R.string.your_attendance_done),false)
            }
        })

        binding.tvUserName.text = sharedPreference.getValueString(Constant.FULL_NAME)
        binding.tvMobile.text = ":  ${sharedPreference.getValueString(Constant.MOBILE)}"
        binding.tvRegisterTo.text = "${getString(R.string.register_to)} V : ${getVersion()}"

        registerReceiver(ConnectivityReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        setupNaviation()
        prepareMenuData()
        populateExpandableList()
        setObserver()

        binding.ivLogout.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are You Sure Want To Logout?")
                .setCancelText("No")
                .setConfirmText("Yes")
                .setCancelClickListener { sDialog -> sDialog.cancel() }
                .setConfirmClickListener { sDialog ->
                    sharedPreference.clearSharedPreference()
                    startNewActivity(LoginActivity())
                    finish()
                   sDialog.cancel()

                }
                .show()
        }
    }




    fun getDate(time:Long):String {
        var date:Date = Date(time); // *1000 is to convert seconds to milliseconds
        var sdf:SimpleDateFormat  = SimpleDateFormat("dd/MM/yyyy HH:mm"); // the format of your date
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));

        return sdf.format(date);
    }

    private fun checkCurrentDate(){
        df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        c = Calendar.getInstance().time
        val currentDate = df!!.format(c)
        Log.d("current_date",currentDate+sharedPreference.getValueString(Constant.CURRENT_DATE))
        if(currentDate!=sharedPreference.getValueString(Constant.CURRENT_DATE)){
            checkAppVersion()
        }
        sharedPreference.save(Constant.CURRENT_DATE,currentDate)

    }

    private fun setObserver(){
        registrationViewModel.appVersion.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer

            if (responseModel.isNotEmpty()) {
                try {
                    val version = getVersion()
                    Log.d("versionName","$version ${responseModel[0].AppVersion} ${responseModel[0].Compulsory}")
                    if(responseModel[0].Logout.equals("No",true)){
                        if(version?.toFloat()!! < responseModel[0].AppVersion?.toFloat()!! && responseModel[0].Compulsory=="1"){

                            var sDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            sDialog.setTitleText("Update Alert!")
                                .setContentText("Download App New Version")
                                .setConfirmText("Yes")
                                .setConfirmClickListener { sDialog ->
                                    sDialog.cancel()
                                    try {
                                        val viewIntent = Intent(
                                            "android.intent.action.VIEW",
                                            Uri.parse("https://play.google.com/store/apps/details?id=com.mpcl")
                                        )
                                        startActivity(viewIntent)
                                        finish()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                .setCancelClickListener { sDialog->
                                    sDialog.cancel()

                                }
                            sDialog.setCancelable(false)
                            sDialog.show()
                        }else{
                            Log.d("versionName","false")
                        }
                    }else{
                        sharedPreference.clearSharedPreference()
                        startNewActivity(LoginActivity())
                        finish()
                    }



                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }
            } else {
                //showToast("Something wrong! Please try again")
            }
        })
    }

    private fun getVersion(): String? {
        val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        return pInfo.versionName
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        //showNetworkMessage(isConnected)
        if(isConnected){
            managePermissions.checkPermissions()
            if(dialogNoInternet!=null && dialogNoInternet!!.isShowing){
                dialogNoInternet?.dismiss()
            }
        }
        else showPopupMessage(getString(R.string.no_internet),getString(R.string.no_internet_connection),true)

    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
        checkCurrentDate()
    }

    private fun setupNaviation() {

        binding.ivHamburgerMenu.setOnClickListener(View.OnClickListener {
            binding.drawer.openDrawer(GravityCompat.START)
        })
        binding.navView.setItemIconTintList(null)
        binding.appBarTitle// = findViewById(R.id.appBarTitle)
        binding.navView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                /*
                R.id.nav_logout -> {
                }*/
            }
            binding.drawer.closeDrawer(GravityCompat.START)
            true
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showPopupMessage(titleText:String,messageText:String,isFinish:Boolean){
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
            dialogNoInternet?.dismiss()
            if(isFinish) finish()
        })


        dialogNoInternet = materialAlertDialogBuilder.show()


    }
    private fun checkAppVersion(){
        val body = mapOf<String, String>(
            "CID" to sharedPreference.getValueString(
                Constant.COMPANY_ID
            )!!,
            "EMPNO" to sharedPreference.getValueString(
                Constant.EMP_NO
            )!!
        )
        registrationViewModel.checkAppVersion(body)
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun customAlertView() {
        var materialAlertDialogBuilder = MaterialAlertDialogBuilder(this, R.style.PauseDialog)
        var customAlertView = LayoutInflater.from(this).inflate(
            R.layout.select_two_option,
            null,
            false
        )
        val registration: RegularTextView = customAlertView!!.findViewById(R.id.registration)
        val attendance: RegularTextView = customAlertView!!.findViewById(R.id.attendance)
        materialAlertDialogBuilder.setView(customAlertView)
        materialAlertDialogBuilder.background = getDrawable(R.drawable.card_view)
        registration.setOnClickListener(View.OnClickListener {
            dialogOption.dismiss()
            registraionDialog()
            //startActivity(Intent(this, RegistrationActivity::class.java))
        })
        attendance.setOnClickListener(View.OnClickListener {
            dialogOption.dismiss()
            Log.d(
                TAG,
                ciphertextWrapper.toString() + " : " + SampleAppUser.fakeToken + " : " + sharedPreference.getUser()
            )
            if (ciphertextWrapper != null && sharedPreference.getUser() != null) {
                showBiometricPromptForDecryption()
            } else {
                dialogOption.dismiss()
                registraionDialog()
            }

            /*if (ciphertextWrapper != null) {
                if (SampleAppUser.fakeToken != null) {
                    showBiometricPromptForDecryption()
                } else {
                    // The user has already logged in, so proceed to the rest of the app
                    // this is a todo for you, the developer
                    //updateApp(getString(R.string.already_signedin))
                    startNewActivity(DashboardActivity())
                }
            }*/
        })
        dialogOption = materialAlertDialogBuilder.show()
        //materialAlertDialogBuilder.show()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun registraionDialog() {
        var materialAlertDialogBuilder = MaterialAlertDialogBuilder(this, R.style.PauseDialog)
        var customAlertView = LayoutInflater.from(this).inflate(
            R.layout.registration_dialog,
            null,
            false
        )
        materialAlertDialogBuilder.background = getDrawable(R.drawable.card_view)
        materialAlertDialogBuilder.setView(customAlertView)
        var binding = RegistrationDialogBinding.bind(customAlertView)
        binding.companyId.setText(sharedPreference.getValueString(Constant.COMPANY_ID).toString())
        binding.empCode.doAfterTextChanged {
            registrationViewModel.onLoginDataChanged(
                binding.companyId.text.toString().trim(),
                binding.empCode.text.toString().trim()
            )
        }
        binding.next.setOnClickListener {
            dialogRegitration.dismiss()
            /*sharedPreference.save(Constant.USER_LOGIN, Gson().toJson(registaionResponseModel))
            sharedPreference.save(Constant.COMPANY_ID, binding.companyId.text.toString())*/
            showBiometricPromptForEncryption()
        }


        registrationViewModel.registrationResponsse.observe(this, Observer {
            hideDialog()
            val responseModel = it ?: return@Observer

            //it[0].branchName?.let { it1 -> Log.d(TAG, it1) }
            if (responseModel.size > 0) {
                registaionResponseModel = responseModel.get(0)
                binding.textInputLayoutName.visibility = View.VISIBLE
                binding.textInputLayoutBranch.visibility = View.VISIBLE
                binding.next.visibility = View.VISIBLE
                binding.name.setText(registaionResponseModel.empName)
                binding.branch.setText(registaionResponseModel.branchName)
            } else {
                //showToast("Something wrong! Please try again")
                binding.textInputLayoutName.visibility = View.GONE
                binding.textInputLayoutBranch.visibility = View.GONE
                binding.next.visibility = View.GONE
            }
        })

        registrationViewModel.loginForm.observe(this, Observer {
            val loginResponseModel = it ?: return@Observer
            when (loginResponseModel) {
                is SuccessfulLoginFormState -> {
                    binding.next.isEnabled = loginResponseModel.isDataValid
                    /*binding.textInputLayoutName.visibility = View.VISIBLE
                    binding.textInputLayoutBranch.visibility = View.VISIBLE*/
                    //binding.next.visibility = View.VISIBLE
                    showDialog()
                    val body = mapOf<String, String>(
                        "CID" to binding.companyId.text.toString().trim(),
                        "EMPNO" to binding.empCode.text.toString().trim()
                    )
                    registrationViewModel.registration(body)

                }
                is FailedLoginFormState -> {
                    loginResponseModel.usernameError?.let {
                        binding.companyId.error = getString(it)
                    }
                    loginResponseModel.passwordError?.let { binding.empCode.error = getString(it) }
                    binding.textInputLayoutName.visibility = View.GONE
                    binding.textInputLayoutBranch.visibility = View.GONE
                    binding.next.visibility = View.GONE

                }
            }
        })

        registrationViewModel.employeeVerificationResponsse.observe(this, Observer {
            val responseModel = it ?: return@Observer
            Log.d(TAG, responseModel.get(0).Response.toString())
            //it[0].branchName?.let { it1 -> Log.d(TAG, it1) }

        })


        dialogRegitration = materialAlertDialogBuilder.show()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(p0: View?) {
        when(p0?.id){
            binding.cvWebview?.id->startActivity(Intent(this,WebViewActivity::class.java))
            binding.cvPickup?.id->startNewActivity(PickupActivity())
            binding.cvTodo?.id->startNewActivity(TodoActivity())
            binding.cvPincodeFinder?.id->startNewActivity(PincodeFinderActivity())
            binding.cvTracking?.id->startNewActivity(FinderActivity())
        }
    }

    private fun showBiometricPromptForEncryption() {
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val secretKeyName = getString(R.string.secret_key_name)
            cryptographyManager = CryptographyManager()
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(this, ::encryptAndStoreServerToken)
            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            //SampleAppUser.fakeToken?.let { token ->
                //Log.d(TAG, "The token from server is $token")
                registaionResponseModel.empCode?.let { empCode->
                    val encryptedServerTokenWrapper = cryptographyManager.encryptData(empCode, this)
                    cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                        encryptedServerTokenWrapper,
                        applicationContext,
                        Constant.SHARED_PREFS_FILENAME,
                        Context.MODE_PRIVATE,
                        Constant.CIPHERTEXT_WRAPPER
                    )
                    sharedPreference.save(
                        Constant.USER_LOGIN,
                        Gson().toJson(registaionResponseModel)
                    )

                    showPopupMessage(getString(R.string.success),getString(R.string.your_registration_completed),false)
                }

            //}
        }

        //finish()
    }

    private fun showBiometricPromptForDecryption() {
        ciphertextWrapper?.let { textWrapper ->
            val secretKeyName = getString(R.string.secret_key_name)
            val cipher = cryptographyManager.getInitializedCipherForDecryption(
                secretKeyName, textWrapper.initializationVector
            )
            biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    this,
                    ::decryptServerTokenFromStorage
                )
            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val plaintext =
                    cryptographyManager.decryptData(textWrapper.ciphertext, it)
                registaionResponseModel?.let { register ->
                    val body = mapOf<String, String>(
                        "CID" to sharedPreference.getValueString(
                            Constant.COMPANY_ID
                        )!!,
                        "BID" to register.bid.toString(),
                        "EMPNO" to register.empCode.toString(),
                        "DTIME" to getDateTime(),
                        "DEVICEID" to getDeviceId(),
                        "MOBILENO" to sharedPreference.getValueString(Constant.MOBILE)!!
                    )
                    registrationViewModel.employeeVerification(body)
                }

                //SampleAppUser.fakeToken = plaintext
                Log.d(TAG, plaintext)

                // Now that you have the token, you can query server for everything else
                // the only reason we call this fakeToken is because we didn't really get it from
                // the server. In your case, you will have gotten it from the server the first time
                // and therefore, it's a real token.

                //updateApp(getString(R.string.already_signedin))

            }
        }
    }

    private fun populateExpandableList() {
        var expandableListAdapter = ExpandableListAdapter(this, headerList, childList)
        binding.expandableListView.setAdapter(expandableListAdapter)
        binding.expandableListView.setOnGroupClickListener(OnGroupClickListener { parent, v, groupPosition, id ->
            if (headerList[groupPosition].isGroup) {
                if (headerList[groupPosition].hasMenu) {
                    var image = v.findViewById<ImageView>(R.id.arrow)
                    if (!binding.expandableListView.isGroupExpanded(groupPosition)) {
                        image.rotation = 180f
                    } else {
                        image.rotation = 0f
                    }

                }
            }else if(!headerList[groupPosition].hasMenu) {
                startNewActivity(EkartLocationActivity())

                if (binding.drawer.isDrawerOpen(GravityCompat.END)) {
                    binding.drawer.closeDrawer(GravityCompat.END)
                }
                //onBackPressed()
            }
            false
        })
        binding.expandableListView.setOnChildClickListener(OnChildClickListener { parent, v, groupPosition, childPosition, id ->
            if (childList[headerList[groupPosition]] != null) {
                val model = childList[headerList[groupPosition]]!![childPosition]
                if (binding.drawer.isDrawerOpen(GravityCompat.END)) {
                    binding.drawer.closeDrawer(GravityCompat.END)
                }
                when (model.name) {
                    "Office Staff" -> {
                        var intent = Intent(this, RegistrationActivity::class.java)
                        intent.putExtra(Constant.ATTENDANCE_TYPE, 1)
                        startActivity(intent)
                    }
                    "Sales Visit" -> {
                        var intent = Intent(this, RegistrationActivity::class.java)
                        intent.putExtra(Constant.ATTENDANCE_TYPE,2)
                        startActivity(intent)
                    }
                    "A/C Copy Upload" -> {
                        var intentDataModel = IntentDataModel(1, null)
                        var intent = Intent(this, AcCopyUploadActivity::class.java)
                        intent.putExtra(Constant.INTENT_TYPE, intentDataModel)
                        startActivity(intent)
                    }
                    "Pod Upload" -> {
                        var intentDataModel = IntentDataModel(2, null)
                        var intent = Intent(this, PODUploadActivity::class.java)
                        intent.putExtra(Constant.INTENT_TYPE, intentDataModel)
                        startActivity(intent)
                    }
                    "Ekart Location" -> {
                        //startActivity(Intent(this,EkartLocationActivity::class.java))
                        //startNewActivity(EkartLocationActivity())
                    }
                    "C Note Entry" -> {
                        showToast("C Note Entry under development")
                    }
                    "Vehicle Load/Unload" -> {
                        startNewActivity(VehicleLoadUploadActivity())
                    }
                    getString(R.string.location_scan) -> {
                        startNewActivity(LocationScanActivity())
                    }
                    "Delivery Update" -> {
                        showToast("Delivery Update under development")
                    }
                    "Deps Upload" -> {
                        showToast("Deps Upload under development")
                    }
                    "Stock Checking" -> {
                        startNewActivity(StockCheckingActivity())
                    }
                    getString(R.string.box_packing) -> {
                        startNewActivity(BoxPackingActivity())
                    }
                    getString(R.string.box_wise_scan) -> {
                        startNewActivity(BoxWiseScanActivity())
                    }
                    getString(R.string.printer_setup) -> {
                        startNewActivity(DeviceSetupActivity())
                    }
                    getString(R.string.sticker_printing) -> {
                        startNewActivity(StickerPrintActivity())
                    }
                    getString(R.string.pickup_scan) -> {
                        startNewActivity(PickupScanActivity())
                    }

                }


            }
            false
        })


    }

    private fun prepareMenuData() {
        var childModelsList: ArrayList<MenuModel> = ArrayList<MenuModel>()

        var menuModel = MenuModel(getString(R.string.attendance), true, true) //Menu of Java Tutorials
        headerList.add(menuModel)
        childModelsList = ArrayList<MenuModel>()
        var childModel = MenuModel(
            "Office Staff",
            false,
            false
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Sales Visit",
            false,
            false
        )
        childModelsList.add(childModel)
        if (menuModel.hasMenu) {
            childList?.put(menuModel, childModelsList)
        }
        childModelsList = ArrayList<MenuModel>()
        menuModel = MenuModel("Operation", true, true) //Menu of Python Tutorials
        headerList.add(menuModel)
        childModel = MenuModel(
            "A/C Copy Upload",
            false,
            false
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Pod Upload",
            false,
            false
        )
        childModelsList.add(childModel)
        /*childModel = MenuModel(
            "Ekart Location",
            false,
            false
        )
        childModelsList.add(childModel)*/
        /*childModel = MenuModel(
            "C Note Entry",
            false,
            false
        )*/
        //childModelsList.add(childModel)
        childModel = MenuModel(
            "Vehicle Load/Unload",
            false,
            false
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            getString(R.string.location_scan),
            false,
            false
        )
        childModelsList.add(childModel)
        /*childModel = MenuModel(
            "Delivery Update",
            false,
            false
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Deps Upload",
            false,
            false
        )
        childModelsList.add(childModel)*/
        childModel = MenuModel(
            "Stock Checking",
            false,
            false
        )
        childModelsList.add(childModel)

        childModel = MenuModel(
            getString(R.string.box_packing),
            false,
            false
        )
        childModelsList.add(childModel)

        childModel = MenuModel(
            getString(R.string.box_wise_scan),
            false,
            false
        )
        childModelsList.add(childModel)

        if (menuModel.hasMenu) {
            childList.put(menuModel, childModelsList)
        }

        menuModel = MenuModel(getString(R.string.ekart), false, false) //Menu of Android Tutorial. No sub menus

        headerList.add(menuModel)
        childModelsList = ArrayList<MenuModel>()
        if (!menuModel.hasMenu) {
            childList.put(menuModel, childModelsList)
        }
        childModel = MenuModel(
            getString(R.string.ekart_location),
            false,
            false
        )
        childModelsList.add(childModel)

        childModel = MenuModel(
            getString(R.string.flipkart),
            false,
            false
        )
        childModelsList.add(childModel)


        childModelsList = ArrayList<MenuModel>()
        menuModel = MenuModel(getString(R.string.bar_code_setting), true, true) //Menu of Python Tutorials
        headerList.add(menuModel)
        childModel = MenuModel(
            getString(R.string.printer_setup),
            false,
            false
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            getString(R.string.sticker_printing),
            false,
            false
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            getString(R.string.pickup_scan),
            false,
            false
        )
        childModelsList.add(childModel)
        if (menuModel.hasMenu) {
            childList.put(menuModel, childModelsList)
        }
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}