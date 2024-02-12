package com.mpcl.activity.operation.boxpacking

data class BoxPackingRequestModel(var CID:String,
                                  var BID:String,
                                  var EMPCODE:String,
                                  var PICKUPNO:String,
                                  var CNOTENO:String,
                                  var SEALNO:String,
                                  var MASTERBOX:String,
                                  var TOTALBOX:String,
                                  var DATASTR:ArrayList<Data>){
    data class Data(var scan_code:String)
}
