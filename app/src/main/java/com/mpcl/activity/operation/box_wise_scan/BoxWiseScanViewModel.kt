package com.mpcl.activity.operation.box_wise_scan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.activity.operation.boxpacking.BoxPackingResponseModel
import com.mpcl.model.PickupResponseModel
import com.mpcl.model.VehicleLoadRequest
import kotlinx.coroutines.launch
import java.lang.Exception

class BoxWiseScanViewModel(var boxWiseScanRepository: BoxWiseScanRepository):ViewModel() {
    var boxPackingResponseModel: MutableLiveData<List<BoxPackingResponseModel>> = MutableLiveData()

    /*fun getPickupExists(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = boxWiseScanRepository.getPickupExists(body)
                pickupExistsResponseModel.value = response
            } catch (e: Exception) {
            }
        }
    }*/

    fun boxScan(boxWiseScanRequestModel: BoxWiseScanRequestModel) {
        viewModelScope.launch() {
            try {
                val response = boxWiseScanRepository.boxScan(boxWiseScanRequestModel)
                boxPackingResponseModel.value = response
            } catch (e: Exception) {
            }
        }
    }
}