package com.mpcl.viewmodel.stickerDataViewModel

import com.mpcl.employee.Network.RetrofitBuilder
import com.mpcl.model.StickerDataResponseModel

class StickerDataRepository {
    suspend fun getStickerDataList(body:Map<String,String>):List<StickerDataResponseModel> = RetrofitBuilder.api.getStickerDataList(body)
}