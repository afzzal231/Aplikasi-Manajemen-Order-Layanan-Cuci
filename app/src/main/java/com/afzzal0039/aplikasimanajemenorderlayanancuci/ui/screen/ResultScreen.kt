package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afzzal0039.aplikasimanajemenorderlayanancuci.R
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    berat: String,
    paket: String,
    hasJaket: Boolean,
    hasSprei: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val hargaPerKg = if (paket == "Reguler") 5000 else 8000
    val biayaKiloan = (berat.toDoubleOrNull() ?: 0.0) * hargaPerKg
    val tambahanBiaya = (if (hasJaket) 10000 else 0) + (if (hasSprei) 15000 else 0)
    val total = biayaKiloan + tambahanBiaya

    val rupiahFormat = NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("id-ID")
    )
    val totalFormatted = rupiahFormat.format(total).replace("Rp", "Rp ")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.hasil_hitung)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Detail Pesanan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Berat: $berat kg")
                    Text("Paket: $paket")

                    if (hasJaket) Text("Jaket: Ya")
                    if (hasSprei) Text("Sprei: Ya")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Total: $totalFormatted",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val shareText =
                                "Estimasi biaya laundry saya ($berat kg, $paket): $totalFormatted"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.btn_bagikan))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kembali")
                    }
                }
            }
        }
    }
}