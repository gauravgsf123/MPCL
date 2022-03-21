package com.mpcl.viewmodel.VehicleLoanUnloadViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.*
import kotlinx.coroutines.launch
import java.lang.Exception

class VechileLoanUnloadViewModel (var vechileLoanUnloadRepository: VechileLoanUnloadRepository): ViewModel() {
    var docTypeListResponse : MutableLiveData<List<DocTypeListResponseModel>> = MutableLiveData()
    var scanDocDataResponse : MutableLiveData<List<ScanDocDataResponseModel>> = MutableLiveData()
    var scanDocTotalResponse : MutableLiveData<List<ScanDocTotalResponseModel>> = MutableLiveData()
    var uploadVehicleScanResponse : MutableLiveData<List<APIResponse>> = MutableLiveData()
    var vehicleResponseModel : MutableLiveData<List<VehicleResponseModel>> = MutableLiveData()

    fun getDocTypeList(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = vechileLoanUnloadRepository.getDocTypeList(body)
                docTypeListResponse.value = response
            }catch (e: Exception){}
        }
    }
    fun scanDocData(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = vechileLoanUnloadRepository.scanDocData(body)
                scanDocDataResponse.value = response
            }catch (e: Exception){}
        }
    }

    fun scanDocTotal(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = vechileLoanUnloadRepository.scanDocTotal(body)
                scanDocTotalResponse.value = response
            }catch (e: Exception){}
        }
    }

    fun uploadVehicleScan(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = vechileLoanUnloadRepository.uploadVehicleScan(body)
                uploadVehicleScanResponse.value = response
            }catch (e: Exception){}
        }
    }

    fun getVehicleDataList(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = vechileLoanUnloadRepository.getVehicleDataList(body)
                vehicleResponseModel.value = response
            }catch (e: Exception){}
        }
    }

}