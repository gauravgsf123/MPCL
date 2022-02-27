package com.mpcl.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ScanLocationResponseModel(@Expose @SerializedName("Response") var Response:String?,
                                     @Expose @SerializedName("Message") var Message:String?)
