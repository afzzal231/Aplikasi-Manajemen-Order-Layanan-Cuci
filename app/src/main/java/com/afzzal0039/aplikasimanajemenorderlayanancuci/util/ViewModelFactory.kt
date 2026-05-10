package com.afzzal0039.aplikasimanajemenorderlayanancuci.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.afzzal0039.aplikasimanajemenorderlayanancuci.database.OrderDao
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.LaundryViewModel

class ViewModelFactory(
    private val dao: OrderDao,
    private val dataStore: SettingsDataStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaundryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LaundryViewModel(dao, dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}