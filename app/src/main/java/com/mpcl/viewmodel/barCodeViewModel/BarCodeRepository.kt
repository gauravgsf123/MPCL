package com.mpcl.viewmodel.barCodeViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.APIResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class BarCodeRepository {
    suspend fun uploadAcCopyData(filePart: MultipartBody.Part,
                              cid: RequestBody?,
                                 empNo: RequestBody?,
                              mobile: RequestBody?,
                              docNo: RequestBody?,
                              deviceImei: RequestBody?): List<APIResponse> = RetrofitBuilder.api.uploadAcCopyData(filePart,cid,empNo,mobile,docNo,deviceImei)

    suspend fun uploadPODCopyData(filePart: MultipartBody.Part,
                                 cid: RequestBody?,
                                  empNo: RequestBody?,
                                 mobile: RequestBody?,
                                 docNo: RequestBody?,
                                 deviceImei: RequestBody?): List<APIResponse> = RetrofitBuilder.api.uploadPODCopyData(filePart,cid,empNo,mobile,docNo,deviceImei)

}