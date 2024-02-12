package com.mpcl.activity.pickup

import com.google.gson.annotations.SerializedName

data class PODDelayReasonResponse(@SerializedName("DELINTERNAL" ) var DELINTERNAL : ArrayList<String> = arrayListOf(),
                                  @SerializedName("UNDEXTERNAL" ) var UNDEXTERNAL : ArrayList<String> = arrayListOf(),
                                  @SerializedName("UNDINTERNAL" ) var UNDINTERNAL : ArrayList<String> = arrayListOf(),
                                  @SerializedName("DELEXTERNAL" ) var DELEXTERNAL : ArrayList<String> = arrayListOf()
)
