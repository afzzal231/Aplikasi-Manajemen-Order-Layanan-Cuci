package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.afzzal0039.aplikasimanajemenorderlayanancuci.R
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.LaundryViewModel
import java.text.NumberFormat
import java.util.Locale

fun shareOrder(context: Context, order: Order, hargaFormatted: String) {
    val shareText = """
        🧺 *Detail Pesanan Laundry Aja* 🧺
        ----------------------------------
        Nama Pelanggan : ${order.namaPelanggan}
        Paket Layanan  : ${order.paketLayanan}
        Berat Pakaian  : ${order.berat} kg
        Estimasi Selesai: ${order.estimasiSelesai}
        ----------------------------------
        *Total Biaya: $hargaFormatted*
        
        Terima kasih telah menggunakan jasa kami!
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Bagikan via"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: LaundryViewModel
) {
    val orders by viewModel.allOrders.collectAsState()
    var orderToDelete by remember { mutableStateOf<Order?>(null) }
    val isGridView by viewModel.isGridView.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Riwayat Pesanan",
                        color = MaterialTheme.colorScheme.onPrimaryContainer // Warna teks kontras
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer // Warna ikon kontras
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleLayout(!isGridView) }) {
                        Icon(
                            painter = painterResource(
                                id = if (isGridView) R.drawable.baseline_view_list_24 else R.drawable.baseline_grid_view_24
                            ),
                            contentDescription = "Switch View",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer, // WARNA BIRU MUDA (Sesuai Gambar)
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada data pesanan.")
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            isGrid = true,
                            onDelete = { orderToDelete = order },
                            onEdit = { navController.navigate(Screen.Edit.createRoute(order.id)) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            isGrid = false,
                            onDelete = { orderToDelete = order },
                            onEdit = { navController.navigate(Screen.Edit.createRoute(order.id)) }
                        )
                    }
                }
            }
        }

        if (orderToDelete != null) {
            AlertDialog(
                onDismissRequest = { orderToDelete = null },
                title = { Text("Hapus Pesanan") },
                text = { Text("Yakin ingin menghapus pesanan milik ${orderToDelete?.namaPelanggan}?") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            orderToDelete?.let { viewModel.deleteOrder(it) }
                            orderToDelete = null
                        }
                    ) { Text("Hapus") }
                },
                dismissButton = {
                    TextButton(onClick = { orderToDelete = null }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    isGrid: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION") val localeID = Locale("in", "ID")
    val rupiahFormat = NumberFormat.getCurrencyInstance(localeID)
    val hargaFormatted = rupiahFormat.format(order.totalHarga).replace("Rp", "Rp ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = order.namaPelanggan,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Text(text = "Paket: ${order.paketLayanan}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "${order.berat} kg", style = MaterialTheme.typography.bodySmall)

            Text(
                text = "Estimasi: ${order.estimasiSelesai}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = hargaFormatted,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isGrid) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { shareOrder(context, order, hargaFormatted) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Bagikan",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (!isGrid) {
                    IconButton(onClick = onEdit) {
                        Icon(painter = painterResource(id = R.drawable.outline_edit_24), contentDescription = "Edit", tint = Color.Blue)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(painter = painterResource(id = R.drawable.outline_delete_24), contentDescription = "Hapus", tint = Color.Red)
                    }
                } else {
                    Row {
                        TextButton(onClick = onEdit, contentPadding = PaddingValues(4.dp)) {
                            Text("Edit", style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = onDelete, contentPadding = PaddingValues(4.dp)) {
                            Text("Hapus", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}