package com.mpcl.viewmodel.registerDevice

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.RegisterDeviceResponse

class RegisterDeviceRepository {
    suspend fun registerDevice(body:Map<String,String>):List<RegisterDeviceResponse> = RetrofitBuilder.api.registerDevice(body)
}