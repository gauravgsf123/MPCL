package com.mpcl.viewmodel.registerDevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RegisterDeviceViewModelFactory(var registerDeviceRepository: RegisterDeviceRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        RegisterDeviceViewModel(registerDeviceRepository) as T
}