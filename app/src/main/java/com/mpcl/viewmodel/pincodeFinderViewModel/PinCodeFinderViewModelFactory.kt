package com.mpcl.viewmodel.pincodeFinderViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PinCodeFinderViewModelFactory(var pinCodeFinderRepository: PinCodeFinderRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        PinCodeFinderViewModel(pinCodeFinderRepository) as T
}