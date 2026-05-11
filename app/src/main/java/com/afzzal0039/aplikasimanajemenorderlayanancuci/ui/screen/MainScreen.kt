package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.afzzal0039.aplikasimanajemenorderlayanancuci.R
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.LaundryViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: LaundryViewModel,
    onAboutClick: () -> Unit
) {
    var namaPelanggan by rememberSaveable { mutableStateOf("") }
    var berat by rememberSaveable { mutableStateOf("") }
    var paket by rememberSaveable { mutableStateOf("Reguler") }
    var hasJaket by rememberSaveable { mutableStateOf(false) }
    var hasSprei by rememberSaveable { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var mDisplayMenu by remember { mutableStateOf(false) }

    val categories by viewModel.allCategories.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val totalHarga = remember(berat, paket, hasJaket, hasSprei, categories) {
        val beratDouble = berat.toDoubleOrNull() ?: 0.0
        val hargaKategori = categories.find { it.name == paket }?.price ?: 5000

        var total = (beratDouble * hargaKategori).toInt()
        if (hasJaket) total += 10000
        if (hasSprei) total += 15000
        total
    }

    @Suppress("DEPRECATION") val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
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
                title = { Text("LaundryAja") },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme(!isDarkMode) }) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkMode) R.drawable.baseline_light_mode_24
                                else R.drawable.baseline_dark_mode_24
                            ),
                            contentDescription = "Ganti Tema"
                        )
                    }

                    Box {
                        IconButton(onClick = { mDisplayMenu = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_menu_24),
                                contentDescription = "Buka Menu"
                            )
                        }

                        DropdownMenu(
                            expanded = mDisplayMenu,
                            onDismissRequest = { mDisplayMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Riwayat Pesanan") },
                                onClick = {
                                    mDisplayMenu = false
                                    navController.navigate(Screen.History.route)
                                },
                                leadingIcon = {
                                    Icon(painterResource(id = R.drawable.outline_history_24), null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Recycle Bin") },
                                onClick = {
                                    mDisplayMenu = false
                                    navController.navigate(Screen.RecycleBin.route)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Tentang Aplikasi") },
                                onClick = {
                                    mDisplayMenu = false
                                    onAboutClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.laundry),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

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
            if (isError) {
                Text("Input tidak valid!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Layanan Tambahan:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = hasJaket, onCheckedChange = { hasJaket = it })
                Text("Jaket (+Rp 10.000)")
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = hasSprei, onCheckedChange = { hasSprei = it })
                Text("Sprei (+Rp 15.000)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pilih Paket:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                categories.forEach { category ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (paket == category.name),
                            onClick = { paket = category.name }
                        )
                        Text(category.name)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Estimasi Biaya:", style = MaterialTheme.typography.bodyMedium)
                    Text(totalFormatted, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = {
                    if (berat.isEmpty() || namaPelanggan.isEmpty() || berat.toDoubleOrNull() == null) {
                        isError = true
                    } else {
                        showDialog = true
                    }
                },
                modifier = Modifier.padding(top = 24.dp).fillMaxWidth()
            ) {
                Text("Simpan Pesanan")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Konfirmasi Simpan") },
                    text = { Text("Simpan pesanan atas nama $namaPelanggan?") },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.insertOrder(
                                nama = namaPelanggan,
                                berat = berat,
                                isJaket = hasJaket,
                                isSprei = hasSprei,
                                paket = paket,
                                total = totalHarga,
                                estimasi = hitungEstimasi(paket)
                            )
                            showDialog = false
                            navController.navigate(Screen.History.route)
                        }) { Text("Ya, Simpan") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Batal") }
                    }
                )
            }
        }
    }
}