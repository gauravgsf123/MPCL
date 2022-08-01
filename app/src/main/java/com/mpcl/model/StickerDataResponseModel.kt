package com.mpcl.model

import com.google.gson.annotations.SerializedName

data class StickerDataResponseModel(@SerializedName("Response") var Response    : String,
                                    @SerializedName("Consignee") var Consignee   : String,
                                    @SerializedName("CompRefNo") var CompRefNo   : String,
                                    @SerializedName("Destination") var Destination : String,
                                    @SerializedName("TotalBox") var TotalBox    : Int,
                                    @SerializedName("CNoteNo") var CNoteNo     : String,
                                    @SerializedName("BarCodeNo") var BarCodeNo   : String,
                                    @SerializedName("Shortcode") var Shortcode   : String,
                                    @SerializedName("Location") var Location   : String,
                                    var printDone:Boolean=false){
}
