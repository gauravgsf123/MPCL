package com.mpcl.activity.operation.boxpacking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.PickupResponseModel
import com.mpcl.model.VehicleLoadRequest
import kotlinx.coroutines.launch
import java.lang.Exception

class BoxPackingViewModel(var boxPackingRepository: BoxPackingRepository):ViewModel() {
    var pickupExistsResponseModel: MutableLiveData<List<PickupExistsResponseModel>> = MutableLiveData()
    var boxPackingResponseModel: MutableLiveData<List<BoxPackingResponseModel>> = MutableLiveData()

    fun getPickupExists(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = boxPackingRepository.getPickupExists(body)
                pickupExistsResponseModel.value = response
            } catch (e: Exception) {
            }
        }
    }

    fun boxPacking(boxPackingRequestModel: BoxPackingRequestModel) {
        viewModelScope.launch() {
            try {
                val response = boxPackingRepository.boxPacking(boxPackingRequestModel)
                boxPackingResponseModel.value = response
            } catch (e: Exception) {
            }
        }
    }
}