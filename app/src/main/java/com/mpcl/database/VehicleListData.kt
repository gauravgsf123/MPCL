package com.mpcl.database

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class VehicleListData(@Id var Id: Long = 0,
                           var Response    : String? = null,
                           var Destination : String? = null,
                           var CNoteNo     : String? = null,
                           var BarCodeNo   : String? = null,
                           var ChgWeight   : String? = null,
                           var isScan   : Boolean? = false)
