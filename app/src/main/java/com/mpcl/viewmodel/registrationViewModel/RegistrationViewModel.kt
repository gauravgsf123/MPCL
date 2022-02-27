package com.mpcl.viewmodel.registrationViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.R
import com.mpcl.model.APIResponse
import com.mpcl.model.AppVersionResponse
import com.mpcl.model.RegistrationResponseModel
import com.mpcl.model.SaveDailyAttendResponseModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class RegistrationViewModel(private val registrationRepositoty: RegistrationRepositoty):ViewModel() {
    val loginForm = MutableLiveData<LoginFormState>()
    val registrationResponsse = MutableLiveData<List<RegistrationResponseModel>>()
    val employeeVerificationResponsse : MutableLiveData<List<SaveDailyAttendResponseModel>> = MutableLiveData()
    val markAttendanceResponse : MutableLiveData<List<APIResponse>> = MutableLiveData()
    val appVersion : MutableLiveData<List<AppVersionResponse>> = MutableLiveData()

    fun registration(body:Map<String,String>){
        viewModelScope.launch{
            try{
                val response = registrationRepositoty.registraion(body)
                registrationResponsse.value = response

            }catch (e:Exception){
                Log.d("RegistrationViewModel", "registration: ${e.message}")
            }
        }
    }

    fun markAttendance(filePart: MultipartBody.Part,
                         cid: RequestBody?,
                         empNo: RequestBody?,
                         bid: RequestBody?,
                         imeiNo: RequestBody?,
                         mobileNo: RequestBody?){
        viewModelScope.launch {
            try{
                val response = registrationRepositoty.markAttendance(filePart, cid, empNo, bid, imeiNo, mobileNo)
                markAttendanceResponse.value = response

            }catch (e:Exception){
                Log.d("RegistrationViewModel", "getPost: ${e.message}")
            }
        }
    }

    fun markSalesAttendance(filePart: MultipartBody.Part,
                       cid: RequestBody?,
                       empNo: RequestBody?,
                       bid: RequestBody?,
                       imeiNo: RequestBody?,
                       mobileNo: RequestBody?){
        viewModelScope.launch {
            try{
                val response = registrationRepositoty.markSalesAttendance(filePart, cid, empNo, bid, imeiNo, mobileNo)
                markAttendanceResponse.value = response

            }catch (e:Exception){
                Log.d("RegistrationViewModel", "getPost: ${e.message}")
            }
        }
    }



    fun employeeVerification(body:Map<String,String>){
        viewModelScope.launch{
            try{
                val response = registrationRepositoty.employeeVerification(body)
                employeeVerificationResponsse.value = response

            }catch (e:Exception){
                Log.d("RegistrationViewModel", "registration: ${e.message}")
            }
        }
    }

    fun checkAppVersion(body:Map<String,String>){
        viewModelScope.launch{
            try{
                val response = registrationRepositoty.checkAppVersion(body)
                appVersion.value = response

            }catch (e:Exception){
                Log.d("RegistrationViewModel", "registration: ${e.message}")
            }
        }
    }

    fun onLoginDataChanged(comId: String, empId: String) {
        /*if (!isUserNameValid(comId)) {
            loginForm.value = FailedLoginFormState(usernameError = R.string.required_field)
        } else*/ if (!isPasswordValid(empId)) {
            loginForm.value = FailedLoginFormState(passwordError = R.string.required_field)
        } else {
            //registration(getMap(comId,empId))
            loginForm.value = SuccessfulLoginFormState(isDataValid = true)
        }
    }

    private fun getMap(comId: String, empId: String):Map<String,String>{
        return mapOf<String,String>("CID" to comId, "EMPNO" to empId)
    }

    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank() /*if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }*/
    }

    // A placeholder password validation check
    private fun isPasswordValid(empcode: String): Boolean {
        return empcode.isNotBlank() && empcode.length==7//password.length > 5
    }
}