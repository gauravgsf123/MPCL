package com.mpcl.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class APIResponse(@Expose @SerializedName("Response") var Response:String?)