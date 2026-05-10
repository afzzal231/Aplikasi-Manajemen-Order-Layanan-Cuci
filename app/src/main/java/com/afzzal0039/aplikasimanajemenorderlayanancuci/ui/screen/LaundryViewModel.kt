package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afzzal0039.aplikasimanajemenorderlayanancuci.database.OrderDao
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LaundryViewModel(private val dao: OrderDao) : ViewModel() {

    val allOrders: StateFlow<List<Order>> = dao.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            viewModelScope.launch {
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

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            dao.deleteOrder(order)
        }
    }
}