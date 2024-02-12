package com.mpcl.model

import com.google.gson.annotations.SerializedName

class PinCodeFinderResponseModel(
    @SerializedName("DistName"  ) var DistName  : String? = null,
    @SerializedName("DelBranch" ) var DelBranch : String? = null,
    @SerializedName("DelTat"    ) var DelTat    : String? = null,
    @SerializedName("CityName"  ) var CityName  : String? = null,
    @SerializedName("Disabled"  ) var Disabled  : String? = null,
    @SerializedName("StatName"  ) var StatName  : String? = null,
    @SerializedName("Oda"       ) var Oda       : String? = null,
    @SerializedName("Pincode"   ) var Pincode   : String? = null,
    @SerializedName("Distance"  ) var Distance  : String? = null,
    @SerializedName("Response"  ) var Response  : String? = null
) {
}