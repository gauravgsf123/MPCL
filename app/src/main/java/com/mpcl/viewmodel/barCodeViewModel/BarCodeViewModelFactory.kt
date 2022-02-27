package com.mpcl.viewmodel.barCodeViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BarCodeViewModelFactory(private val barCodeRepository: BarCodeRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        BarCodeViewModel(barCodeRepository) as T
}