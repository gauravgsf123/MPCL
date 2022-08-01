package com.mpcl.viewmodel.VehicleLoanUnloadViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.*
import com.mpcl.network.RetrofitInstance
import retrofit2.Response

class VechileLoanUnloadRepository {

    suspend fun getDocTypeList(body:Map<String,String>):List<DocTypeListResponseModel>? = RetrofitInstance.apiService?.getDocTypeList(body)

    suspend fun scanDocData(body:Map<String,String>):List<ScanDocDataResponseModel>? = RetrofitInstance.apiService?.scanDocData(body)

    suspend fun scanDocTotal(body:Map<String,String>):List<ScanDocTotalResponseModel>? = RetrofitInstance.apiService?.scanDocTotal(body)

    suspend fun uploadVehicleScan(body:Map<String,String>):List<APIResponse>? = RetrofitInstance.apiService?.uploadVehicleScan(body)

    //suspend fun registerDevice(body:Map<String,String>): Response<List<RegisterDeviceResponse>>? = RetrofitInstance.apiService?.uploadVehicleScan(body)

    suspend fun getVehicleDataList(body:Map<String,String>):List<VehicleResponseModel>? = RetrofitInstance.apiService?.getVehicleDataList(body)

    suspend fun uploadNewVehicleScan(vecicleloadRequst: VehicleLoadRequest):List<ScanDocDataResponseModel>? = RetrofitInstance.apiService?.uploadNewVehicleScan(vecicleloadRequst)

    suspend fun sendExtraScan(body:Map<String,String>): List<SendExtraScanResponseModel>? = RetrofitInstance.apiService?.sendExtraScan(body)
}