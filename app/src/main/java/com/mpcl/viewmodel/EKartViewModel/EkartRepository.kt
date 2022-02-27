package com.mpcl.viewmodel.EKartViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.EkartResponseModel

class EkartRepository {
    suspend fun getEKartList(): List<EkartResponseModel>? = RetrofitBuilder.api.getEKartList()
}