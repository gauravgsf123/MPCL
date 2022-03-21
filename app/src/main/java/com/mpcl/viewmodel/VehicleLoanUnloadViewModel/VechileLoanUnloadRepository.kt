package com.mpcl.viewmodel.VehicleLoanUnloadViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.*

class VechileLoanUnloadRepository {

    suspend fun getDocTypeList(body:Map<String,String>):List<DocTypeListResponseModel> = RetrofitBuilder.api.getDocTypeList(body)

    suspend fun scanDocData(body:Map<String,String>):List<ScanDocDataResponseModel> = RetrofitBuilder.api.scanDocData(body)

    suspend fun scanDocTotal(body:Map<String,String>):List<ScanDocTotalResponseModel> = RetrofitBuilder.api.scanDocTotal(body)

    suspend fun uploadVehicleScan(body:Map<String,String>):List<APIResponse> = RetrofitBuilder.api.uploadVehicleScan(body)

    suspend fun getVehicleDataList(body:Map<String,String>):List<VehicleResponseModel> = RetrofitBuilder.api.getVehicleDataList(body)
}