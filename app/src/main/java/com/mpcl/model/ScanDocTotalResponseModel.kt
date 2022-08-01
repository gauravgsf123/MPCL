package com.mpcl.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ScanDocTotalResponseModel(@Expose @SerializedName("Response") var Response:String?,
                                     @Expose @SerializedName("ScanBox") var ScanBox:Int?,
                                     @Expose @SerializedName("TotalBox") var TotalBox:Int?,
                                     @Expose @SerializedName("CNoteNo") var CNoteNo:String?)