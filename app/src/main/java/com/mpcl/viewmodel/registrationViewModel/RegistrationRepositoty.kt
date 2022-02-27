package com.mpcl.viewmodel.registrationViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.APIResponse
import com.mpcl.model.AppVersionResponse
import com.mpcl.model.RegistrationResponseModel
import com.mpcl.model.SaveDailyAttendResponseModel
import okhttp3.MultipartBody
import okhttp3.RequestBody

class RegistrationRepositoty {
    suspend fun registraion(body: Map<String, String>): List<RegistrationResponseModel> =
        RetrofitBuilder.api.registration(body)

    suspend fun employeeVerification(body: Map<String, String>): List<SaveDailyAttendResponseModel> =
        RetrofitBuilder.api.employeeVerification(body)

    suspend fun markAttendance(
        filePart: MultipartBody.Part,
        cid: RequestBody?,
        empNo: RequestBody?,
        bid: RequestBody?,
        imeiNo: RequestBody?,
        mobileNo: RequestBody?
    ): List<APIResponse> =
        RetrofitBuilder.api.markAttendance(filePart, cid, empNo, bid, imeiNo, mobileNo)

    suspend fun markSalesAttendance(
        filePart: MultipartBody.Part,
        cid: RequestBody?,
        empNo: RequestBody?,
        bid: RequestBody?,
        imeiNo: RequestBody?,
        mobileNo: RequestBody?
    ): List<APIResponse> =
        RetrofitBuilder.api.markSalesAttendance(filePart, cid, empNo, bid, imeiNo, mobileNo)

    suspend fun checkAppVersion(body: Map<String, String>): List<AppVersionResponse> =
        RetrofitBuilder.api.checkAppVersion(body)
}