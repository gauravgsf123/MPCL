package com.mpcl.viewmodel.pickViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.BranchListResponseModel
import com.mpcl.model.PickupResponseModel
import com.mpcl.model.ScanLocationResponseModel
import com.mpcl.network.RetrofitInstance

class PickupRepository {
    suspend fun pickupRequest(body:Map<String,String>):List<PickupResponseModel>? = RetrofitInstance.apiService?.pickupRequest(body)

}