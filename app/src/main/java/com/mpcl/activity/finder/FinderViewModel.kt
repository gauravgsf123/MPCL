package com.mpcl.activity.finder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.lang.Exception

class FinderViewModel(var finderRepository: FinderRepository):ViewModel() {
    var finderResponse: MutableLiveData<List<FinderResponseModel>> = MutableLiveData()


    fun getTrackData(body: Map<String, String>) {
        viewModelScope.launch() {
            try {
                val response = finderRepository.getTrackData(body)
                finderResponse.value = response
            } catch (e: Exception) {
            }
        }
    }
}