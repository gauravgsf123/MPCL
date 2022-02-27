package com.mpcl.database


import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class StockCheckingDB(@Id var Id: Long = 0,
                      var bar_code: String? = null)