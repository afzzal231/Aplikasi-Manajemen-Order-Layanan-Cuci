package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afzzal0039.aplikasimanajemenorderlayanancuci.database.OrderDao
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import com.afzzal0039.aplikasimanajemenorderlayanancuci.util.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LaundryViewModel(
    private val dao: OrderDao,
    private val dataStore: SettingsDataStore
) : ViewModel() {

    val allOrders: StateFlow<List<Order>> = dao.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isGridView: StateFlow<Boolean> = dataStore.isGridLayout
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleLayout(isGrid: Boolean) {
        viewModelScope.launch {
            dataStore.saveLayoutSetting(isGrid)
        }
    }

    fun insertOrder(
        nama: String,
        berat: String,
        isJaket: Boolean,
        isSprei: Boolean,
        paket: String,
        total: Int,
        estimasi: String
    ) {
        val beratFloat = berat.toFloatOrNull() ?: 0f

        if (nama.isNotBlank() && beratFloat > 0f) {
            viewModelScope.launch(Dispatchers.IO) {
                val order = Order(
                    namaPelanggan = nama,
                    berat = beratFloat,
                    isJaket = isJaket,
                    isSprei = isSprei,
                    paketLayanan = paket,
                    totalHarga = total,
                    estimasiSelesai = estimasi
                )
                dao.insertOrder(order)
            }
        }
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateOrder(order)
        }
    }

    suspend fun getOrderById(id: Int): Order? {
        return withContext(Dispatchers.IO) {
            dao.getOrderById(id)
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteOrder(order)
        }
    }
}