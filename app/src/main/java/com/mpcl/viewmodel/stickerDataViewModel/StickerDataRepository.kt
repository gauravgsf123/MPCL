package com.mpcl.viewmodel.stickerDataViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.StickerDataResponseModel
import com.mpcl.network.RetrofitInstance

class StickerDataRepository {
    suspend fun getStickerDataList(body:Map<String,String>):List<StickerDataResponseModel>? = RetrofitInstance.apiService?.getStickerDataList(body)
}