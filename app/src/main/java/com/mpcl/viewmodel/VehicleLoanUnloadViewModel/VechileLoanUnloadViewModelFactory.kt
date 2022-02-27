package com.mpcl.viewmodel.VehicleLoanUnloadViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VechileLoanUnloadViewModelFactory (var vechileLoanUnloadRepository: VechileLoanUnloadRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        VechileLoanUnloadViewModel(vechileLoanUnloadRepository) as T
}