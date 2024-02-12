package com.mpcl.activity.operation.boxpacking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BoxPackingViewModelFactory (var boxPackingRepository: BoxPackingRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        BoxPackingViewModel(boxPackingRepository) as T
}