package com.mpcl.viewmodel.EKartViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EkartViewModelFactory(val ekartRepository: EkartRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        EkartViewModel(ekartRepository) as T
}