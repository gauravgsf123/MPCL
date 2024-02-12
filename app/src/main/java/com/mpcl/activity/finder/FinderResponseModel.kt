package com.mpcl.activity.finder

import com.google.gson.annotations.SerializedName

data class FinderResponseModel(@SerializedName("Response"      ) var Response      : String? = null,
                               @SerializedName("ORIGIN"        ) var ORIGIN        : String? = null,
                               @SerializedName("EDDDATE"       ) var EDDDATE       : String? = null,
                               @SerializedName("CONSIGNOR"     ) var CONSIGNOR     : String? = null,
                               @SerializedName("CNOTEDATE"     ) var CNOTEDATE     : String? = null,
                               @SerializedName("DESTINATION"   ) var DESTINATION   : String? = null,
                               @SerializedName("ADDATE"        ) var ADDATE        : String? = null,
                               @SerializedName("CONSIGNEE"     ) var CONSIGNEE     : String? = null,
                               @SerializedName("CURRENTSTATUS" ) var CURRENTSTATUS : String? = null,
                               @SerializedName("CNOTENO"       ) var CNOTENO       : String? = null)
