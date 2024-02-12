package com.mpcl.viewmodel.pincodeFinderViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.*
import com.mpcl.network.RetrofitInstance

class PinCodeFinderRepository {
    suspend fun getPinCodeFinder(body:Map<String,String>):List<PinCodeFinderResponseModel>? = RetrofitInstance.apiService?.getPinCodeFinder(body)

}