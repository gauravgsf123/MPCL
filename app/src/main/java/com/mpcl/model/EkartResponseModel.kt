package com.mpcl.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EkartResponseModel(@Expose @SerializedName("srno") var srno:String?,
                              @Expose @SerializedName("lcode") var lcode:String?,
                              @Expose @SerializedName("location") var location:String?)
