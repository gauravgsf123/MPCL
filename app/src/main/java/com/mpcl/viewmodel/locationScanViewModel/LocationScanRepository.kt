package com.mpcl.viewmodel.locationScanViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.BranchListResponseModel
import com.mpcl.model.ScanLocationResponseModel

class LocationScanRepository {
    suspend fun getBranchList(body:Map<String,String>):List<BranchListResponseModel> = RetrofitBuilder.api.getBranchList(body)

    suspend fun scanLocation(cid:String,bid:String,empno:String,boxnumber:String):List<ScanLocationResponseModel> = RetrofitBuilder.api.scanLocation(cid,bid,empno,boxnumber)
}