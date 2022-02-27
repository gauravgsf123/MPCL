package com.mpcl.database

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class EkartDB(@Id var Id: Long = 0,
              var srno: String? = null,
              var lcode: String? = null,
              var location: String? = null)