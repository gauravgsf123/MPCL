package com.mpcl.database

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class RegistrationDB(@Id var Id: Long = 0,
                          var fakeToken: String? = null,
                          var empCode: String? = null,
                          var empName: String? = null,
                          var branchName: String? = null,
                          var bid:String?=null) {
}