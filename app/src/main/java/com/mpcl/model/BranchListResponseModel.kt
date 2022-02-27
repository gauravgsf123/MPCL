package com.mpcl.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BranchListResponseModel(@Expose @SerializedName("bid") var bid:String?,
                                   @Expose @SerializedName("branch") var branch:String?)
