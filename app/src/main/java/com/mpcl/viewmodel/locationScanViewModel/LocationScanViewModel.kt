package com.mpcl.viewmodel.locationScanViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.BranchListResponseModel
import com.mpcl.model.ScanLocationResponseModel
import kotlinx.coroutines.launch
import java.lang.Exception

class LocationScanViewModel (var locationScanRepository: LocationScanRepository): ViewModel() {
    var branchListResponse: MutableLiveData<List<BranchListResponseModel>> = MutableLiveData()
    var scanLocationResponse: MutableLiveData<List<ScanLocationResponseModel>> = MutableLiveData()
    fun getBranchList(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = locationScanRepository.getBranchList(body)
                branchListResponse.value = response
            } catch (e: Exception) {
            }
        }
    }

    fun scanLocation(cid:String,bid:String,empno:String,boxnumber:String,branchName:String,location:String) {
        viewModelScope.launch() {
            try {
                val response = locationScanRepository.scanLocation(cid,bid,empno,boxnumber,branchName,location)
                scanLocationResponse.value = response
            } catch (e: Exception) {
            }
        }
    }
}