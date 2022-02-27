package com.mpcl.viewmodel.stickerDataViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StickerDataViewModelFactory(var stickerDataRepository: StickerDataRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        StickerDataViewModel(stickerDataRepository) as T
}