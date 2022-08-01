package com.mpcl.viewmodel.pickViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PickupViewModelFactory(var pickupRepository: PickupRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        PickupViewModel(pickupRepository) as T
}