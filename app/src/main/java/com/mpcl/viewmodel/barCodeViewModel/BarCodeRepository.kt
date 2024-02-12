package com.mpcl.viewmodel.barCodeViewModel

import com.mpcl.activity.pickup.PODDelayReasonResponse
import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.APIResponse
import com.mpcl.model.CNoteDateLimitResponse
import com.mpcl.model.PodDateLimitResponse
import com.mpcl.network.Apis
import com.mpcl.network.RetrofitInstance
import okhttp3.MultipartBody
import okhttp3.RequestBody

class BarCodeRepository {
    suspend fun uploadAcCopyData(filePart: MultipartBody.Part,
                              cid: RequestBody?,
                                 empNo: RequestBody?,
                              mobile: RequestBody?,
                              bid: RequestBody?,
                              docNo: RequestBody?,
                              deviceImei: RequestBody?): List<APIResponse>? = RetrofitInstance.apiService?.uploadAcCopyData(filePart,cid,empNo,mobile,bid,docNo,deviceImei)

    suspend fun uploadPODCopyData(filePart: MultipartBody.Part,
                                 cid: RequestBody?,
                                  empNo: RequestBody?,
                                 mobile: RequestBody?,
                                 bid: RequestBody?,
                                 docNo: RequestBody?,
                                 date: RequestBody?,
                                 deviceImei: RequestBody?,
                                  statusType: RequestBody?,
                                  reasonType: RequestBody?,
                                  reason: RequestBody?,
                                  cNoteNumber: RequestBody?): List<APIResponse>? = RetrofitInstance.apiService?.uploadPODCopyData(filePart,cid,empNo,mobile,bid,docNo,date,deviceImei,statusType, reasonType, reason, cNoteNumber)

    suspend fun uploadPODCopyData(filePart: MultipartBody.Part,
                                  cid: RequestBody?,
                                  empNo: RequestBody?,
                                  mobile: RequestBody?,
                                  bid: RequestBody?,
                                  docNo: RequestBody?,
                                  date: RequestBody?,
                                  deviceImei: RequestBody?): List<APIResponse>? = RetrofitInstance.apiService?.uploadPODCopyData(filePart,cid,empNo,mobile,bid,docNo,date,deviceImei)

    suspend fun getLimitDate(body: Map<String, String>): List<PodDateLimitResponse>? = RetrofitInstance.apiService?.getLimitDate(body)

    suspend fun delayReason(body:Map<String,String>):List<PODDelayReasonResponse>? = RetrofitInstance.apiService?.delayReason(body)

}