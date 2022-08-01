package com.mpcl.model

data class VehicleLoadRequest(var CID:String,var BID:String,
                              var DOCNUMBER:String,var DOCTYPE:String,
                              var EMPNO:String,var data:ArrayList<Data>){
    data class Data(var scan_code:String)
}
