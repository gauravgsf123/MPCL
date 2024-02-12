package com.mpcl.viewmodel.pickViewModel

import com.mpcl.activity.pickup.PODDelayReasonResponse
import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.*
import com.mpcl.network.RetrofitInstance

class PickupRepository {
    suspend fun pickupRequest(body:Map<String,String>):List<PickupResponseModel>? = RetrofitInstance.apiService?.pickupRequest(body)
    suspend fun pickupTypeReason(body:Map<String,String>):List<PickupReasonResponseModel>? = RetrofitInstance.apiService?.pickupTypeReason(body)
    suspend fun pickupSave(body:Map<String,String>):List<SaveDailyAttendResponseModel>? = RetrofitInstance.apiService?.pickupSave(body)
    suspend fun delayReason(body:Map<String,String>):List<PODDelayReasonResponse>? = RetrofitInstance.apiService?.delayReason(body)

}