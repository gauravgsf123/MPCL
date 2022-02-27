package com.mpcl.viewmodel.barCodeViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.APIResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class BarCodeViewModel(private val barCodeRepository: BarCodeRepository):ViewModel() {
    val responseBarCode : MutableLiveData<List<APIResponse>> = MutableLiveData()
    fun uploadAcCopyData(filePart: MultipartBody.Part,
                      cid: RequestBody?,
                         empNo: RequestBody?,
                      mobile: RequestBody?,
                      docNo: RequestBody?,
                      deviceImei: RequestBody?){
        viewModelScope.launch {
            try{
                val response = barCodeRepository.uploadAcCopyData(filePart,cid,empNo,mobile,docNo,deviceImei)
                responseBarCode.value = response

            }catch (e:Exception){
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }

    fun uploadPODCopyData(filePart: MultipartBody.Part,
                         cid: RequestBody?,
                          empNo: RequestBody?,
                         mobile: RequestBody?,
                         docNo: RequestBody?,
                         deviceImei: RequestBody?){
        viewModelScope.launch {
            try{
                val response = barCodeRepository.uploadPODCopyData(filePart,cid,empNo,mobile,docNo,deviceImei)
                responseBarCode.value = response

            }catch (e:Exception){
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }
}