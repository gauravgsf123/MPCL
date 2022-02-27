package com.mpcl.viewmodel.EKartViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.EkartResponseModel
import kotlinx.coroutines.launch
import java.lang.Exception

class EkartViewModel(val ekartRepository: EkartRepository):ViewModel() {
    val eKartResponse : MutableLiveData<List<EkartResponseModel>> = MutableLiveData()

    fun getEKartList(){
        viewModelScope.launch {
            try {
                val response = ekartRepository.getEKartList()
                eKartResponse.value = response
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}