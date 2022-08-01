package com.mpcl.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DocTypeListResponseModel(@Expose @SerializedName("Value") var Value:String?,
                                    @Expose @SerializedName("Prefix") var Prefix:String?,
                                    @Expose @SerializedName("Name") var Name:String?)
