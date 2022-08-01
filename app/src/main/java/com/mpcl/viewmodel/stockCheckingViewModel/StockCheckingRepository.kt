package com.mpcl.viewmodel.stockCheckingViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.APIResponse
import com.mpcl.network.RetrofitInstance

class StockCheckingRepository {
    suspend fun uploadStockChecking(body:String):List<APIResponse>? = RetrofitInstance.apiService?.uploadStockChecking(body)
}