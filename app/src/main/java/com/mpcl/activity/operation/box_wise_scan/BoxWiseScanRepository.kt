package com.mpcl.activity.operation.box_wise_scan

import com.mpcl.activity.operation.boxpacking.BoxPackingResponseModel
import com.mpcl.model.ScanDocDataResponseModel
import com.mpcl.model.VehicleLoadRequest
import com.mpcl.network.RetrofitInstance

class BoxWiseScanRepository {
    suspend fun boxScan(boxWiseScanRequestModel: BoxWiseScanRequestModel):List<BoxPackingResponseModel>? = RetrofitInstance.apiService?.boxScan(boxWiseScanRequestModel)
}