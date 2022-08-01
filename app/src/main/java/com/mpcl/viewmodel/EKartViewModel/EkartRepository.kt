package com.mpcl.viewmodel.EKartViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.EkartResponseModel
import com.mpcl.network.RetrofitInstance

class EkartRepository {
    suspend fun getEKartList(): List<EkartResponseModel>? = RetrofitInstance.apiService?.getEKartList()
}