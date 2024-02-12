package com.mpcl.viewmodel.barCodeViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.activity.pickup.PODDelayReasonResponse
import com.mpcl.model.APIResponse
import com.mpcl.model.CNoteDateLimitResponse
import com.mpcl.model.PodDateLimitResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class BarCodeViewModel(private val barCodeRepository: BarCodeRepository) : ViewModel() {
    val responseBarCode: MutableLiveData<List<APIResponse>> = MutableLiveData()
    val responseLimitDate: MutableLiveData<List<PodDateLimitResponse>> = MutableLiveData()
    var podDelayReasonResponse: MutableLiveData<List<PODDelayReasonResponse>> = MutableLiveData()
    fun uploadAcCopyData(
        filePart: MultipartBody.Part,
        cid: RequestBody?,
        empNo: RequestBody?,
        mobile: RequestBody?,
        bid: RequestBody?,
        docNo: RequestBody?,
        deviceImei: RequestBody?
    ) {
        viewModelScope.launch {
            try {
                val response = barCodeRepository.uploadAcCopyData(
                    filePart,
                    cid,
                    empNo,
                    mobile,
                    bid,
                    docNo,
                    deviceImei
                )
                responseBarCode.value = response

            } catch (e: Exception) {
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }

    fun uploadPODCopyData(
        filePart: MultipartBody.Part,
        cid: RequestBody?,
        empNo: RequestBody?,
        mobile: RequestBody?,
        bid: RequestBody?,
        docNo: RequestBody?,
        date: RequestBody?,
        deviceImei: RequestBody?,
        statusType: RequestBody?,
        reasonType: RequestBody?,
        reason: RequestBody?,
        cNoteNumber: RequestBody?
    ) {
        viewModelScope.launch {
            try {
                val response = barCodeRepository.uploadPODCopyData(
                    filePart,
                    cid,
                    empNo,
                    mobile,
                    bid,
                    docNo,
                    date,
                    deviceImei,
                    statusType,
                    reasonType,
                    reason,
                    cNoteNumber
                )
                responseBarCode.value = response

            } catch (e: Exception) {
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }

    fun uploadPODCopyData(
        filePart: MultipartBody.Part,
        cid: RequestBody?,
        empNo: RequestBody?,
        mobile: RequestBody?,
        bid: RequestBody?,
        docNo: RequestBody?,
        date: RequestBody?,
        deviceImei: RequestBody?
    ) {
        viewModelScope.launch {
            try {
                val response = barCodeRepository.uploadPODCopyData(
                    filePart,
                    cid,
                    empNo,
                    mobile,
                    bid,
                    docNo,
                    date,
                    deviceImei
                )
                responseBarCode.value = response

            } catch (e: Exception) {
                Log.d("main", "getPost: ${e.message}")
            }
        }
    }

    fun getLimitDate(body: Map<String, String>) {
        viewModelScope.launch {
            var response = barCodeRepository.getLimitDate(body)
            responseLimitDate.value = response
        }
    }

    fun delayReason(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = barCodeRepository.delayReason(body)
                podDelayReasonResponse.value = response
            } catch (e: java.lang.Exception) {
            }
        }
    }
}