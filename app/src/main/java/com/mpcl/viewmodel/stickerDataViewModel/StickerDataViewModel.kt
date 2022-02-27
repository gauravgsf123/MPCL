package com.mpcl.viewmodel.stickerDataViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.StickerDataResponseModel
import kotlinx.coroutines.launch
import java.lang.Exception

class StickerDataViewModel(var stickerDataRepository: StickerDataRepository): ViewModel() {
    var stickerDataList : MutableLiveData<List<StickerDataResponseModel>> = MutableLiveData()

    fun getStickerDataList(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = stickerDataRepository.getStickerDataList(body)
                stickerDataList.value = response
            }catch (e: Exception){}
        }
    }
}