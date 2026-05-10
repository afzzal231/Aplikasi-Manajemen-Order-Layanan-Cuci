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
        }
    }

    val hargaPerKg = if (paket == "Reguler") 5000 else 8000
    val totalHarga = remember(berat, paket, hasJaket, hasSprei) {
        val beratDouble = berat.toDoubleOrNull() ?: 0.0
        var total = (beratDouble * hargaPerKg).toInt()
        if (hasJaket) total += 10000
        if (hasSprei) total += 15000
        total
    }

    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    val totalFormatted = rupiahFormat.format(totalHarga).replace("Rp", "Rp ")

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
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
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
                onValueChange = { namaPelanggan = it },
                label = { Text("Nama Pelanggan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = berat,
                onValueChange = { berat = it; isError = false },
                label = { Text("Berat (Kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Layanan Tambahan:", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasJaket, onCheckedChange = { hasJaket = it })
                Text("Jaket (+Rp 10.000)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasSprei, onCheckedChange = { hasSprei = it })
                Text("Sprei (+Rp 15.000)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pilih Paket:", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = (paket == "Reguler"), onClick = { paket = "Reguler" })
                Text("Reguler")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = (paket == "Ekspres"), onClick = { paket = "Ekspres" })
                Text("Ekspres")
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
                    if (berat.isEmpty() || namaPelanggan.isEmpty() || berat.toFloatOrNull() == null) {
                        isError = true
                    } else {
                        viewModel.updateOrder(
                            Order(
                                id = orderId,
                                namaPelanggan = namaPelanggan,
                                berat = berat.toFloat(),
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