package com.mpcl.employee.Network

import com.mpcl.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface Api
{
    //@FormUrlEncoded
    @GET("setup/mRegister.htm")
    suspend fun registerDevice(@QueryMap loginMap: Map<String, String>): List<RegisterDeviceResponse>

    @GET("emp/mEmpVerifyDetail.htm")
    suspend fun registration(@QueryMap loginMap: Map<String, String>): List<RegistrationResponseModel>

    @GET("emp/mSaveDailyAttend.htm")
    suspend fun employeeVerification(@QueryMap loginMap: Map<String, String>): List<SaveDailyAttendResponseModel>

    @Multipart
    @POST("emp/mSaveDailyAttend.htm")
    suspend fun markAttendance(@Part dataFile: MultipartBody.Part,
                                 @Part("CID") CID: RequestBody?,
                                 @Part("EMPNO") EMPNO: RequestBody?,
                                 @Part("BID") BID: RequestBody?,
                                 @Part("IMEINO") IMEINO: RequestBody?,
                                 @Part("MOBILENO") MOBILENO: RequestBody?): List<APIResponse>

    @Multipart
    @POST("sale/mSaveSalesAttend.htm")
    suspend fun markSalesAttendance(@Part dataFile: MultipartBody.Part,
                                 @Part("CID") CID: RequestBody?,
                                 @Part("EMPNO") EMPNO: RequestBody?,
                                 @Part("BID") BID: RequestBody?,
                                 @Part("IMEINO") IMEINO: RequestBody?,
                                 @Part("MOBILENO") MOBILENO: RequestBody?): List<APIResponse>

    @Multipart
    @POST("operation/mAcCopyUpload.htm")
    suspend fun uploadAcCopyData(@Part selfi: MultipartBody.Part,
                               @Part("CID") psId: RequestBody?,
                                 @Part("EMPNO") EMPNO: RequestBody?,
                               @Part("MOBILENO") userId: RequestBody?,
                               @Part("DOCKETNO") locId: RequestBody?,
                               @Part("IMEINO") routeId: RequestBody?): List<APIResponse>

    @Multipart
    @POST("operation/mPodCopyUpload.htm")
    suspend fun uploadPODCopyData(@Part selfi: MultipartBody.Part,
                                 @Part("CID") psId: RequestBody?,
                                  @Part("EMPNO") EMPNO: RequestBody?,
                                 @Part("MOBILENO") userId: RequestBody?,
                                 @Part("DOCKETNO") locId: RequestBody?,
                                 @Part("IMEINO") routeId: RequestBody?): List<APIResponse>

    /*@POST("viewAllLocation")
    suspend fun getAllLocation(@Body body: Map<String, String>): AllLocationListModel*/

    @GET("operation/mEkartLocation.htm")
    suspend fun getEKartList(): List<EkartResponseModel>

    @FormUrlEncoded
    @POST("operation/mStockCheck.htm")
    suspend fun uploadStockChecking(@Field("DATASTR") DATASTR: String): List<APIResponse>

    @GET("operation/mBranchList.htm")
    suspend fun getBranchList(@QueryMap loginMap: Map<String, String>): List<BranchListResponseModel>

    @FormUrlEncoded
    @POST("operation/mScanLocation.htm")
    suspend fun scanLocation(@Field("CID") CID: String,
                             @Field("BID") BID: String,
                             @Field("EMPNO") EMPNO: String,
                             @Field("BOXNUMBER") BOXNUMBER: String): List<ScanLocationResponseModel>

    @GET("operation/mDocTypeList.htm")
    suspend fun getDocTypeList(@QueryMap loginMap: Map<String, String>): List<DocTypeListResponseModel>


    @POST("operation/mScanDocData.htm")
    suspend fun scanDocData(@QueryMap loginMap: Map<String, String>): List<ScanDocDataResponseModel>


    @POST("operation/mScanDocTotal.htm")
    suspend fun scanDocTotal(@QueryMap loginMap: Map<String, String>): List<ScanDocTotalResponseModel>


    @POST("operation/mVehicleScan.htm")
    suspend fun uploadVehicleScan(@QueryMap loginMap: Map<String, String>) : List<APIResponse>

    @POST("operation/mStickerData.htm")
    suspend fun getStickerDataList(@QueryMap loginMap: Map<String, String>): List<StickerDataResponseModel>

    @GET("setup/mApiVersion.htm")
    suspend fun checkAppVersion(@QueryMap cid: Map<String, String>): List<AppVersionResponse>

    @POST("operation/mVehicleData.htm")
    suspend fun getVehicleDataList(@QueryMap loginMap: Map<String, String>): List<VehicleResponseModel>







}