package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch { dataStore.saveDarkMode(isDark) }
    }

    fun toggleLayout(isGrid: Boolean) {
        viewModelScope.launch { dataStore.saveLayoutSetting(isGrid) }
    }

    fun insertOrder(nama: String, berat: String, isJaket: Boolean, isSprei: Boolean, paket: String, total: Int, estimasi: String, imageUri: String?, imageFile: File?) {
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
                    isDeleted = false,
                    imageUri = imageUri
                )
                dao.insertOrder(order)

                if (imageFile != null) {
                    val namaLayananBody = paket.toRequestBody("text/plain".toMediaTypeOrNull())
                    val ketBody = nama.toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                    withContext(Dispatchers.Main) {
                        addOrderToApi(namaLayananBody, ketBody, body)
                    }
                }
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

    fun fetchOrdersFromApi() {
        viewModelScope.launch {
            _apiState.value = ApiState.Loading
            try {
                val user = userFlow.first()
                if (user.email.isNotEmpty()) {
                    val remoteData = LaundryApi.retrofitService.getOrders(user.email)

                    withContext(Dispatchers.IO) {
                        dao.clearAllOrders()
                        dao.insertAll(remoteData)
                    }
                    _apiState.value = ApiState.Success
                } else {
                    _apiState.value = ApiState.Error("User belum login")
                }
            } catch (e: Exception) {
                _apiState.value = ApiState.Error(e.message ?: "Terjadi kesalahan jaringan")
            }
        }
    }

    fun addOrderToApi(namaLayanan: RequestBody, keterangan: RequestBody, image: MultipartBody.Part) {
        viewModelScope.launch {
            _apiState.value = ApiState.Loading
            try {
                val user = userFlow.first()

                // Cek apakah email kosong
                if (user.email.isEmpty()) {
                    Log.e("API_DEBUG", "GAGAL KIRIM: Email kosong. Anda belum Login Google di aplikasi!")
                    return@launch
                }

                Log.d("API_DEBUG", "Mulai mengirim data ke server...")

                val response = LaundryApi.retrofitService.postOrder(user.email, namaLayanan, keterangan, image)

                if (response.status == "success") {
                    Log.d("API_DEBUG", "BERHASIL: Data sukses masuk ke database!")
                    fetchOrdersFromApi()
                } else {
                    Log.e("API_DEBUG", "DITOLAK SERVER: ${response.message}")
                    _apiState.value = ApiState.Error(response.message ?: "Gagal menambah data")
                }
            } catch (e: Exception) {
                Log.e("API_DEBUG", "CRASH/GAGAL KONEKSI: ${e.message}")
                e.printStackTrace()
                _apiState.value = ApiState.Error(e.message ?: "Gagal menghubungi server")
            }
        }
    }

    fun deleteOrderFromApi(orderId: Int) {
        viewModelScope.launch {
            _apiState.value = ApiState.Loading
            try {
                val user = userFlow.first()
                val response = LaundryApi.retrofitService.deleteOrder(user.email, orderId)

                if (response.status == "success") {
                    fetchOrdersFromApi()
                } else {
                    _apiState.value = ApiState.Error(response.message ?: "Gagal menghapus data")
                }
            } catch (e: Exception) {
                _apiState.value = ApiState.Error(e.message ?: "Gagal menghubungi server")
            }
        }
    }

    fun clearApiState() {
        _apiState.value = ApiState.Idle
    }
}