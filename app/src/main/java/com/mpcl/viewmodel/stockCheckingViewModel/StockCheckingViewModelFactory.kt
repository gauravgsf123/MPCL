package com.mpcl.viewmodel.stockCheckingViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StockCheckingViewModelFactory(var stockCheckingRepository: StockCheckingRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        StockCheckingViewModel(stockCheckingRepository) as T
}