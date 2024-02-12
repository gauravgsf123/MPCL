package com.mpcl.viewmodel.pickViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.activity.pickup.PODDelayReasonResponse
import com.mpcl.model.*
import kotlinx.coroutines.launch
import java.lang.Exception

class PickupViewModel (var pickupRepository: PickupRepository): ViewModel() {
    var pickupResponse: MutableLiveData<List<PickupResponseModel>> = MutableLiveData()
    var pickupReasonResponseModel: MutableLiveData<List<PickupReasonResponseModel>> = MutableLiveData()
    var savePickupResponse: MutableLiveData<List<SaveDailyAttendResponseModel>> = MutableLiveData()
    var podDelayReasonResponse: MutableLiveData<List<PODDelayReasonResponse>> = MutableLiveData()
    fun pickupRequest(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = pickupRepository.pickupRequest(body)
                pickupResponse.value = response
            } catch (e: Exception) {
            }
        }
    }

    fun pickupTypeReason(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = pickupRepository.pickupTypeReason(body)
                pickupReasonResponseModel.value = response
            } catch (e: Exception) {
            }
        }
    }

    fun pickupSave(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = pickupRepository.pickupSave(body)
                savePickupResponse.value = response
            } catch (e: Exception) {
            }
        }
    }

    fun delayReason(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = pickupRepository.delayReason(body)
                podDelayReasonResponse.value = response
            } catch (e: Exception) {
            }
        }
    }

}