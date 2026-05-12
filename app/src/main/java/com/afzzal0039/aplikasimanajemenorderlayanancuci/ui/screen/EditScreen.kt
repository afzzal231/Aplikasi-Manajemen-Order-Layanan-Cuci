package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.LaundryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    orderId: Int,
    navController: NavHostController,
    viewModel: LaundryViewModel
) {
    val categories by viewModel.allCategories.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    var namaPelanggan by rememberSaveable { mutableStateOf("") }
    var berat by rememberSaveable { mutableStateOf("") }
    var paket by rememberSaveable { mutableStateOf("Reguler") }
    var hasJaket by rememberSaveable { mutableStateOf(false) }
    var hasSprei by rememberSaveable { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        val order = viewModel.getOrderById(orderId)
        order?.let {
            namaPelanggan = it.namaPelanggan
            berat = it.berat.toString()
            paket = it.paketLayanan
            hasJaket = it.isJaket
            hasSprei = it.isSprei
            isLoading = false
        }
    }

    val totalHarga = remember(berat, paket, hasJaket, hasSprei, categories) {
        val beratDouble = berat.toDoubleOrNull() ?: 0.0
        val hargaKategori = categories.find { it.name == paket }?.price ?: 5000

        var total = (beratDouble * hargaKategori).toInt()
        if (hasJaket) total += 10000
        if (hasSprei) total += 15000
        total
    }

    val totalFormatted = remember(totalHarga) {
        @Suppress("DEPRECATION") val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        rupiahFormat.format(totalHarga).replace("Rp", "Rp ")
    }

    fun hitungEstimasi(paketDipilih: String): String {
        val calendar = Calendar.getInstance()
        if (paketDipilih == "Reguler") calendar.add(Calendar.DAY_OF_YEAR, 2)
        else calendar.add(Calendar.HOUR_OF_DAY, 6)
        return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(calendar.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Pesanan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text("Ubah Data Pesanan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = namaPelanggan,
                    onValueChange = { namaPelanggan = it; isError = false },
                    label = { Text("Nama Pelanggan") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError && namaPelanggan.isEmpty(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = berat,
                    onValueChange = { },
                    label = { Text("Berat (Kg)") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Layanan Tambahan (Tidak dapat diubah):", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasJaket, onCheckedChange = null, enabled = false)
                    Text("Jaket (+Rp 10.000)")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasSprei, onCheckedChange = null, enabled = false)
                    Text("Sprei (+Rp 15.000)")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Pilih Paket Baru:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    categories.forEach { category ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                            RadioButton(
                                selected = (paket == category.name),
                                onClick = { paket = category.name }
                            )
                            Text(category.name)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Biaya Baru:", style = MaterialTheme.typography.bodyMedium)
                        Text(totalFormatted, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Button(
                    onClick = {
                        if (namaPelanggan.isBlank()) {
                            isError = true
                        } else {
                            val beratFloat = berat.toFloatOrNull() ?: 0f
                            viewModel.updateOrder(
                                Order(
                                    id = orderId,
                                    namaPelanggan = namaPelanggan,
                                    berat = beratFloat,
                                    isJaket = hasJaket,
                                    isSprei = hasSprei,
                                    paketLayanan = paket,
                                    totalHarga = totalHarga,
                                    estimasiSelesai = hitungEstimasi(paket)
                                )
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Text("Simpan Perubahan")
                }
            }
        }
    }
}