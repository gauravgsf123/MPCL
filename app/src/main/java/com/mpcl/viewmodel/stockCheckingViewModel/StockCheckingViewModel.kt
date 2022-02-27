package com.mpcl.viewmodel.stockCheckingViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.APIResponse
import kotlinx.coroutines.launch
import java.lang.Exception

class StockCheckingViewModel(var stockCheckingRepository: StockCheckingRepository):ViewModel() {
    var stockCheckingResponse : MutableLiveData<List<APIResponse>> = MutableLiveData()
    fun uploadStockChecking(body:String){
        viewModelScope.launch(){
            try {
                val response = stockCheckingRepository.uploadStockChecking(body)
                stockCheckingResponse.value = response
            }catch (e: Exception){}
        }
    }
}