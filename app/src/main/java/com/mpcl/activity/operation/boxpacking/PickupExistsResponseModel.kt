package com.mpcl.activity.operation.boxpacking

import com.google.gson.annotations.SerializedName

data class PickupExistsResponseModel(
    @SerializedName("Response" ) var Response : String? = null,
    @SerializedName("Message"  ) var Message  : String? = null
)