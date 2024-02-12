package com.mpcl.activity.operation.boxpacking

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BoxPackingResponseModel(@Expose @SerializedName("Response") var Response:String?,
                                   @Expose @SerializedName("Message") var Message:String?)
