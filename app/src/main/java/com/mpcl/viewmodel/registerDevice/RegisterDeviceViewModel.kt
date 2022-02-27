package com.mpcl.viewmodel.registerDevice

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.RegisterDeviceResponse
import kotlinx.coroutines.launch
import java.lang.Exception


class RegisterDeviceViewModel(var registerDeviceRepository: RegisterDeviceRepository):ViewModel() {
    var registerDeviceResponse : MutableLiveData<List<RegisterDeviceResponse>> = MutableLiveData()
    fun registerDevice(body:Map<String,String>){
        viewModelScope.launch(){
            try {
                val response = registerDeviceRepository.registerDevice(body)
                registerDeviceResponse.value = response
            }catch (e:Exception){}
        }
    }
}