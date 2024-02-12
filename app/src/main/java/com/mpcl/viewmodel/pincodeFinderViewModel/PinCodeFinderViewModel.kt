package com.mpcl.viewmodel.pincodeFinderViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mpcl.model.*
import kotlinx.coroutines.launch
import java.lang.Exception

class PinCodeFinderViewModel (var pickupRepository: PinCodeFinderRepository): ViewModel() {
    var pinCodeFinderResponse: MutableLiveData<List<PinCodeFinderResponseModel>> = MutableLiveData()
    fun getPinCodeFinder(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = pickupRepository.getPinCodeFinder(body)
                pinCodeFinderResponse.value = response
            } catch (e: Exception) {
            }
        }
    }

}