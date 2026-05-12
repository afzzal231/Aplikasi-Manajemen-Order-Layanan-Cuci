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
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.Screen
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


fun shareOrder(context: Context, order: Order, hargaFormatted: String) {
    val shareText = """
          *Detail Pesanan Laundry Aja*
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pesanan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleLayout(!isGridView) }) {
                        Icon(
                            painter = painterResource(
                                id = if (isGridView) R.drawable.baseline_view_list_24 else R.drawable.baseline_grid_view_24
                            ),
                            contentDescription = "Switch View"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada data pesanan aktif.", style = MaterialTheme.typography.bodyLarge)
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
                    items(orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onDelete = { orderToDelete = order },
                            onEdit = {
                                navController.navigate(Screen.Edit.createRoute(order.id))
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(
                            order = order,
                            onDelete = { orderToDelete = order },
                            onEdit = {
                                navController.navigate(Screen.Edit.createRoute(order.id))
                            }
                        )
                    }
                }
            }
        }

        if (orderToDelete != null) {
            AlertDialog(
                onDismissRequest = { orderToDelete = null },
                icon = { Icon(painterResource(id = R.drawable.outline_delete_24), contentDescription = null, tint = Color.Red) },
                title = { Text("Pindahkan ke Recycle Bin?") },
                text = { Text("Pesanan milik ${orderToDelete?.namaPelanggan} akan dipindahkan ke tempat sampah.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            val currentOrder = orderToDelete!!
                            viewModel.moveToTrash(currentOrder)
                            orderToDelete = null

                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Pindah ke Recycle Bin",
                                    actionLabel = "BATALKAN",
                                    duration = SnackbarDuration.Short
                                )

                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreOrder(currentOrder)
                                }
                            }
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
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val hargaFormatted = remember(order.totalHarga) {
        @Suppress("DEPRECATION") val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        rupiahFormat.format(order.totalHarga).replace("Rp", "Rp ")
    }

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
            Text("Paket: ${order.paketLayanan}", style = MaterialTheme.typography.bodyMedium)
            Text("${order.berat} kg", style = MaterialTheme.typography.bodySmall)
            Text(
                "Estimasi: ${order.estimasiSelesai}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = hargaFormatted,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { shareOrder(context, order, hargaFormatted) }) {
                    Icon(Icons.Default.Share, "Bagikan", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onEdit) {
                    Icon(painterResource(id = R.drawable.outline_edit_24), "Edit", tint = Color.Blue)
                }
                IconButton(onClick = onDelete) {
                    Icon(painterResource(id = R.drawable.outline_delete_24), "Hapus", tint = Color.Red)
                }
            }
        }
    }
}