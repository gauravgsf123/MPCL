package com.mpcl.activity.operation.box_wise_scan

data class BoxWiseScanRequestModel(var CID:String,
                                   var BID:String,
                                   var VEHICLENUMBER:String,
                                   var TYPE:String,
                                   var data:ArrayList<Data>) {
    data class Data(var scan_code: String)
}
