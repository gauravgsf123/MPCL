package com.mpcl.model


import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RegistrationResponseModel(@Expose @SerializedName("empCode") var empCode:String?,
                                     @Expose @SerializedName("empName") var empName:String?,
                                     @Expose @SerializedName("branchName") var branchName:String?,
                                     @Expose @SerializedName("bid") var bid:String?,
                                     @Expose @SerializedName("imeino") var imeino:String?,
                                     @Expose @SerializedName("latitude") var latitude:String?,
                                     @Expose @SerializedName("longitue") var longitue:String?)
