package com.mpcl.activity.operation.boxpacking

import com.mpcl.model.ScanDocDataResponseModel
import com.mpcl.model.VehicleLoadRequest
import com.mpcl.network.RetrofitInstance

class BoxPackingRepository {
    suspend fun getPickupExists(body: Map<String, String>):List<PickupExistsResponseModel>? = RetrofitInstance.apiService?.getPickupExists(body)
    suspend fun boxPacking(boxPackingRequestModel: BoxPackingRequestModel):List<BoxPackingResponseModel>? = RetrofitInstance.apiService?.boxPacking(boxPackingRequestModel)
}