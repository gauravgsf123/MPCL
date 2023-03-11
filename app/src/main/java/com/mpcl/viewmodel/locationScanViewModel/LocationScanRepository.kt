package com.mpcl.viewmodel.locationScanViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.BranchListResponseModel
import com.mpcl.model.ScanLocationResponseModel
import com.mpcl.network.RetrofitInstance

class LocationScanRepository {
    suspend fun getBranchList(body:Map<String,String>):List<BranchListResponseModel>? = RetrofitInstance.apiService?.getBranchList(body)

    suspend fun scanLocation(cid:String,bid:String,empno:String,boxnumber:String,branchName:String,location:String):List<ScanLocationResponseModel>? = RetrofitInstance.apiService?.scanLocation(cid,bid,empno,boxnumber,branchName,location)
}