package com.mpcl.viewmodel.locationScanViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LocationScanViewModelFactory(var locationScanRepository: LocationScanRepository):
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T=
        LocationScanViewModel(locationScanRepository) as T
}