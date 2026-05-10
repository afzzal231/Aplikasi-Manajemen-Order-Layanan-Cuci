package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.afzzal0039.aplikasimanajemenorderlayanancuci.R
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.theme.AplikasiManajemenOrderLayananCuciTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenPreview() {
    AplikasiManajemenOrderLayananCuciTheme {
        MainScreen(
            onCalculate = { _, _, _, _ -> },
            onAboutClick = { }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onCalculate: (String, String, Boolean, Boolean) -> Unit,
    onAboutClick: () -> Unit
) {
    var berat by rememberSaveable { mutableStateOf("") }
    var paket by rememberSaveable { mutableStateOf("Reguler") }
    var isError by remember { mutableStateOf(false) }

    var hasJaket by rememberSaveable { mutableStateOf(false) }
    var hasSprei by rememberSaveable { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }

    val hargaPerKg = if (paket == "Reguler") 5000 else 8000

    val totalHarga = remember(berat, paket, hasJaket, hasSprei) {
        val beratInt = berat.toIntOrNull() ?: 0
        var total = beratInt * hargaPerKg
        if (hasJaket) total += 10000
        if (hasSprei) total += 15000
        total
    }

    val rupiahFormat = NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("id-ID")
    )
    val totalFormatted = remember(totalHarga) {
        rupiahFormat.format(totalHarga).replace("Rp", "Rp ")
    }

    fun hitungEstimasi(paket: String): String {
        val calendar = Calendar.getInstance()
        if (paket == "Reguler") {
            calendar.add(Calendar.DAY_OF_YEAR, 2)
        } else {
            calendar.add(Calendar.HOUR_OF_DAY, 6)
        }
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return format.format(calendar.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = onAboutClick) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                }
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
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = berat,
                onValueChange = { berat = it; isError = false },
                label = { Text(stringResource(id = R.string.label_berat)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isError,
                modifier = Modifier.fillMaxWidth()
            )

            if (isError) {
                Text(
                    stringResource(id = R.string.error_input),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Item Khusus (Satuan):", fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = hasJaket, onCheckedChange = { hasJaket = it })
                Text("Jaket (+Rp 10.000)")
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = hasSprei, onCheckedChange = { hasSprei = it })
                Text("Sprei (+Rp 15.000)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(id = R.string.pilih_paket), fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = (paket == "Reguler"), onClick = { paket = "Reguler" })
                Text("Reguler")

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(selected = (paket == "Ekspres"), onClick = { paket = "Ekspres" })
                Text("Ekspres")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Total: $totalFormatted", fontWeight = FontWeight.Bold)
            Text("Selesai: ${hitungEstimasi(paket)}")

            Button(
                onClick = {
                    if (berat.isEmpty() || berat.toDoubleOrNull() == null) {
                        isError = true
                    } else {
                        showDialog = true
                    }
                },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.btn_hitung))
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Ringkasan Pesanan") },
                    text = {
                        Column {
                            Text("Berat: $berat kg")
                            Text("Jaket: ${if (hasJaket) "Ya" else "Tidak"}")
                            Text("Sprei: ${if (hasSprei) "Ya" else "Tidak"}")
                            Text("Paket: $paket")
                            Text("Estimasi: ${hitungEstimasi(paket)}")
                            Text("Total: $totalFormatted")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDialog = false
                                onCalculate(berat, paket, hasJaket, hasSprei)
                            }
                        ) {
                            Text("Lanjut")
                        }
                    }
                )
            }
        }
    }
}