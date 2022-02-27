package com.mpcl.viewmodel.registrationViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RegistrationViewModelFactory(private val registrationRepositoty: RegistrationRepositoty):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        RegistrationViewModel(registrationRepositoty) as T
}