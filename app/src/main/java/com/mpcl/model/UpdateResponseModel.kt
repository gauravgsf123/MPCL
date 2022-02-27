package com.mpcl.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateResponseModel(@Expose @SerializedName("statusCode") val statusCode : String,
                               @Expose @SerializedName("message") val message : String)
