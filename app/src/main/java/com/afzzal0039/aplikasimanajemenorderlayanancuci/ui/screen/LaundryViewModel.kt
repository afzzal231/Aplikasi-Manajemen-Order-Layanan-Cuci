package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afzzal0039.aplikasimanajemenorderlayanancuci.database.OrderDao
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Category
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

    val allCategories: StateFlow<List<Category>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isDarkMode: StateFlow<Boolean> = dataStore.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isGridView: StateFlow<Boolean> = dataStore.isGridLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        seedCategories()
    }

    private fun seedCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.getCategoryCount() == 0) {
                dao.insertCategory(Category(name = "Reguler", price = 5000))
                dao.insertCategory(Category(name = "Ekspres", price = 8000))
            }
        }
    }

    val allOrders: StateFlow<List<Order>> = dao.getAllActiveOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashOrders: StateFlow<List<Order>> = dao.getTrashOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch { dataStore.saveDarkMode(isDark) }
    }

    fun toggleLayout(isGrid: Boolean) {
        viewModelScope.launch { dataStore.saveLayoutSetting(isGrid) }
    }

    fun insertOrder(nama: String, berat: String, isJaket: Boolean, isSprei: Boolean, paket: String, total: Int, estimasi: String) {
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
                    estimasiSelesai = estimasi,
                    isDeleted = false
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

    fun moveToTrash(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.moveToTrash(order.id)
        }
    }

    fun restoreOrder(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.restoreFromTrash(order.id)
        }
    }

    fun hardDelete(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deletePermanently(order)
        }
    }
}