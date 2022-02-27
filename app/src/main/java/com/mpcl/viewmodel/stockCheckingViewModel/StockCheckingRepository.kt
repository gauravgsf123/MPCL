package com.mpcl.viewmodel.stockCheckingViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.APIResponse

class StockCheckingRepository {
    suspend fun uploadStockChecking(body:String):List<APIResponse> = RetrofitBuilder.api.uploadStockChecking(body)
}