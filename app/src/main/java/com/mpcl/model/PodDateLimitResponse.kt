package com.mpcl.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PodDateLimitResponse(@Expose @SerializedName("Response") var Response:String?,
                                @Expose @SerializedName("DrsDate") var DrsDate:String?)
