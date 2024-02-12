package com.mpcl.activity.finder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FinderViewModelFactory(var finderRepository: FinderRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        FinderViewModel(finderRepository) as T
}