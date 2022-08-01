package com.mpcl.model

data class VehicleResponseModel(var Response    : String? = null,
                                var Destination : String? = null,
                                var CNoteNo     : String? = null,
                                var BarCodeNo   : String? = null,
                                var ChgWeight   : String? = null,
                                var isScan   : Boolean? = false)
