package com.mpcl.activity.finder

import com.mpcl.model.SaveDailyAttendResponseModel
import com.mpcl.network.RetrofitInstance

class FinderRepository {
    suspend fun getTrackData(body:Map<String,String>):List<FinderResponseModel>? = RetrofitInstance.apiService?.getTrackData(body)
}