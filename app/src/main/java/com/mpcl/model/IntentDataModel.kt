package com.mpcl.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IntentDataModel(var type: Int?=0, var value: String?=null):Parcelable{
}