package com.mpcl.viewmodel.registerDevice

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.RegisterDeviceResponse
import com.mpcl.network.RetrofitInstance

class RegisterDeviceRepository {
    suspend fun registerDevice(body:Map<String,String>):List<RegisterDeviceResponse>? = RetrofitInstance.apiService?.registerDevice(body)
}