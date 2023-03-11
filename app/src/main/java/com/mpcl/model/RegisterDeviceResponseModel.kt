package com.mpcl.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RegisterDeviceResponse (

    @Expose @SerializedName("Response") var Response : String?,
    @Expose @SerializedName("FullName") var FullName : String?,
    @Expose @SerializedName("Bid") var Bid : String?,
    @Expose @SerializedName("UserType") var UserType : String?,
    @Expose @SerializedName("OtpNo") var OtpNo : String?

)
