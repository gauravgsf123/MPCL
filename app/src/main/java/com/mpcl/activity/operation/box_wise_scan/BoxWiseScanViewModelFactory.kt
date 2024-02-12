package com.mpcl.activity.operation.box_wise_scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BoxWiseScanViewModelFactory (var boxWiseScanRepository: BoxWiseScanRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        BoxWiseScanViewModel(boxWiseScanRepository) as T
}