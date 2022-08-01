package com.mpcl.viewmodel.pickViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.BranchListResponseModel
import com.mpcl.model.PickupResponseModel
import com.mpcl.model.ScanLocationResponseModel
import kotlinx.coroutines.launch
import java.lang.Exception

class PickupViewModel (var pickupRepository: PickupRepository): ViewModel() {
    var pickupResponse: MutableLiveData<List<PickupResponseModel>> = MutableLiveData()
    var scanLocationResponse: MutableLiveData<List<ScanLocationResponseModel>> = MutableLiveData()
    fun pickupRequest(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = pickupRepository.pickupRequest(body)
                pickupResponse.value = response
            } catch (e: Exception) {
            }
        }
    }

}