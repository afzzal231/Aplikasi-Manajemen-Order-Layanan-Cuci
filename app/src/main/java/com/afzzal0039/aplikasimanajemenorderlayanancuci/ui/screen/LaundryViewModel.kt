package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afzzal0039.aplikasimanajemenorderlayanancuci.database.OrderDao
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Category
import com.afzzal0039.aplikasimanajemenorderlayanancuci.util.SettingsDataStore
import com.afzzal0039.aplikasimanajemenorderlayanancuci.util.UserDataStore
import com.afzzal0039.aplikasimanajemenorderlayanancuci.network.LaundryApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

sealed class ApiState {
    object Idle : ApiState()
    object Loading : ApiState()
    object Success : ApiState()
    data class Error(val message: String) : ApiState()
}

class LaundryViewModel(
    private val dao: OrderDao,
    private val dataStore: SettingsDataStore,
    private val userDataStore: UserDataStore
) : ViewModel() {

    private val _apiState = MutableStateFlow<ApiState>(ApiState.Idle)
    val apiState: StateFlow<ApiState> = _apiState.asStateFlow()

    val userFlow = userDataStore.userFlow

    val allCategories: StateFlow<List<Category>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isDarkMode: StateFlow<Boolean> = dataStore.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isGridView: StateFlow<Boolean> = dataStore.isGridLayout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allOrders: StateFlow<List<Order>> = dao.getAllActiveOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashOrders: StateFlow<List<Order>> = dao.getTrashOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { seedCategories() }

    private fun seedCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.getCategoryCount() == 0) {
                dao.insertCategory(Category(name = "Reguler", price = 5000))
                dao.insertCategory(Category(name = "Ekspres", price = 8000))
            }
        }
    }

    fun toggleTheme(isDark: Boolean) { viewModelScope.launch { dataStore.saveDarkMode(isDark) } }
    fun toggleLayout(isGrid: Boolean) { viewModelScope.launch { dataStore.saveLayoutSetting(isGrid) } }
    suspend fun getOrderById(id: Int): Order? = withContext(Dispatchers.IO) { dao.getOrderById(id) }

    fun insertOrder(nama: String, berat: String, isJaket: Boolean, isSprei: Boolean, paket: String, total: Int, estimasi: String, imageUri: String?, imageFile: File?) {
        val beratFloat = berat.toFloatOrNull() ?: 0f
        if (nama.isNotBlank() && beratFloat > 0f) {
            viewModelScope.launch(Dispatchers.IO) {

                val order = Order(
                    namaPelanggan = nama, berat = beratFloat, isJaket = isJaket, isSprei = isSprei,
                    paketLayanan = paket, totalHarga = total, estimasiSelesai = estimasi, isDeleted = false, imageUri = imageUri
                )
                dao.insertOrder(order)

                if (imageFile != null) {
                    val namaPelangganBody = nama.toRequestBody("text/plain".toMediaTypeOrNull())
                    val paketLayananBody = paket.toRequestBody("text/plain".toMediaTypeOrNull())
                    val beratBody = beratFloat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val totalHargaBody = total.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val estimasiBody = estimasi.toRequestBody("text/plain".toMediaTypeOrNull())
                    val isJaketBody = (if (isJaket) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
                    val isSpreiBody = (if (isSprei) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                    try {
                        val user = userFlow.first()
                        if (user.email.isNotEmpty()) {
                            val response = LaundryApi.retrofitService.postOrder(
                                user.email, namaPelangganBody, paketLayananBody, beratBody, totalHargaBody, estimasiBody, isJaketBody, isSpreiBody, body
                            )
                            if (response.status == "success") {
                                fetchOrdersFromApi()
                            }
                        }
                    } catch (e: Exception) {
                        _apiState.value = ApiState.Error("Data tersimpan offline. Harap klik ikon Sinkronisasi (Pojok Kanan Atas) di Riwayat Pesanan saat internet menyala.")
                    }
                }
            }
        }
    }

    fun syncOfflineOrders(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _apiState.value = ApiState.Loading
            try {
                val user = userFlow.first()
                if (user.email.isEmpty()) return@launch

                val localOrders = dao.getAllActiveOrders().first()

                val offlineOrders = localOrders.filter {
                    it.imageUri != null && !it.imageUri.startsWith("http")
                }

                if (offlineOrders.isEmpty()) {
                    fetchOrdersFromApi()
                    return@launch
                }

                for (order in offlineOrders) {
                    try {
                        val uri = Uri.parse(order.imageUri)
                        val tempFile = java.io.File(context.cacheDir, "sync_image_${System.currentTimeMillis()}.jpg")
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val outputStream = java.io.FileOutputStream(tempFile)
                        inputStream?.copyTo(outputStream)
                        inputStream?.close()
                        outputStream.close()

                        val namaBody = order.namaPelanggan.toRequestBody("text/plain".toMediaTypeOrNull())
                        val paketBody = order.paketLayanan.toRequestBody("text/plain".toMediaTypeOrNull())
                        val beratBody = order.berat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        val hargaBody = order.totalHarga.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        val estimasiBody = order.estimasiSelesai.toRequestBody("text/plain".toMediaTypeOrNull())
                        val isJaketBody = (if (order.isJaket) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
                        val isSpreiBody = (if (order.isSprei) "1" else "0").toRequestBody("text/plain".toMediaTypeOrNull())
                        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)

                        val response = LaundryApi.retrofitService.postOrder(
                            user.email, namaBody, paketBody, beratBody, hargaBody, estimasiBody, isJaketBody, isSpreiBody, body
                        )

                        if (response.status == "success") {
                            dao.deletePermanently(order)
                        }
                    } catch (e: Exception) {
                        Log.e("API_DEBUG", "Gagal upload data offline: ${e.message}")
                    }
                }
                fetchOrdersFromApi()
                _apiState.value = ApiState.Success

            } catch (e: Exception) {
                _apiState.value = ApiState.Error("Gagal Sinkronisasi: Pastikan internet Anda menyala.")
            }
        }
    }

    fun fetchOrdersFromApi() {
        viewModelScope.launch {
            try {
                val user = userFlow.first()
                if (user.email.isNotEmpty()) {
                    val remoteData = LaundryApi.retrofitService.getOrders(user.email)
                    withContext(Dispatchers.IO) {
                        dao.clearAllOrders()
                        dao.insertAll(remoteData)
                    }
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "Server offline: ${e.message}")
            }
        }
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userFlow.first()
                val response = LaundryApi.retrofitService.updateOrder(
                    userId = user.email, action = "update", orderId = order.id, namaPelanggan = order.namaPelanggan,
                    paketLayanan = order.paketLayanan, berat = order.berat, totalHarga = order.totalHarga,
                    estimasiSelesai = order.estimasiSelesai, isJaket = if (order.isJaket) 1 else 0, isSprei = if (order.isSprei) 1 else 0
                )
                if (response.status == "success") {
                    dao.updateOrder(order)
                    fetchOrdersFromApi()
                }
            } catch (e: Exception) { Log.e("API_DEBUG", "Gagal update: ${e.message}") }
        }
    }

    fun moveToTrash(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userFlow.first()
                val response = LaundryApi.retrofitService.updateTrashStatus(user.email, "trash", order.id)
                if (response.status == "success") {
                    dao.moveToTrash(order.id)
                    fetchOrdersFromApi()
                }
            } catch (e: Exception) { Log.e("API_DEBUG", "Gagal hapus: ${e.message}") }
        }
    }

    fun restoreOrder(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userFlow.first()
                val response = LaundryApi.retrofitService.updateTrashStatus(user.email, "restore", order.id)
                if (response.status == "success") {
                    dao.restoreFromTrash(order.id)
                    fetchOrdersFromApi()
                }
            } catch (e: Exception) { Log.e("API_DEBUG", "Gagal mengembalikan: ${e.message}") }
        }
    }

    fun hardDelete(order: Order) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userFlow.first()
                val response = LaundryApi.retrofitService.deleteOrder(user.email, order.id)
                if (response.status == "success") {
                    dao.deletePermanently(order)
                    fetchOrdersFromApi()
                }
            } catch (e: Exception) { Log.e("API_DEBUG", "Gagal hapus permanen: ${e.message}") }
        }
    }

    fun clearApiState() { _apiState.value = ApiState.Idle }
}