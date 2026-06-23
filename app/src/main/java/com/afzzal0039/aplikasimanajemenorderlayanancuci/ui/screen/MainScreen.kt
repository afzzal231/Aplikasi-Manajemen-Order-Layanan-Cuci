package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
// --- IMPORT FITUR PULL TO REFRESH BOX MATERIAL 3 ---
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
// ----------------------------------------------------
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.afzzal0039.aplikasimanajemenorderlayanancuci.BuildConfig
import com.afzzal0039.aplikasimanajemenorderlayanancuci.R
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.User
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.ApiState
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.LaundryViewModel
import com.afzzal0039.aplikasimanajemenorderlayanancuci.util.UserDataStore
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: LaundryViewModel,
    userDataStore: UserDataStore,
    onAboutClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val user by userDataStore.userFlow.collectAsState(initial = User("", "", ""))

    val apiState by viewModel.apiState.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }

    var isAuthLoading by remember { mutableStateOf(false) }
    var isPullRefreshing by remember { mutableStateOf(false) } // Mendeteksi tarikan layar

    var namaPelanggan by rememberSaveable { mutableStateOf("") }
    var berat by rememberSaveable { mutableStateOf("") }
    var paket by rememberSaveable { mutableStateOf("Reguler") }
    var hasJaket by rememberSaveable { mutableStateOf(false) }
    var hasSprei by rememberSaveable { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isError by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val categories by viewModel.allCategories.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) imageUri = result.uriContent
    }

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

    LaunchedEffect(apiState) {
        if (apiState !is ApiState.Loading) {
            isPullRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LaundryAja") },
                actions = {
                    IconButton(onClick = { showProfileDialog = true }) {
                        if (user.email.isEmpty()) Icon(Icons.Default.AccountCircle, null)
                        else AsyncImage(model = user.photoUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isPullRefreshing,
            onRefresh = {
                isPullRefreshing = true
                viewModel.syncOfflineOrders(context)
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Image(painter = painterResource(id = R.drawable.laundry), contentDescription = null, modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally))

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = namaPelanggan, onValueChange = { namaPelanggan = it }, label = { Text("Nama Pelanggan") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = berat, onValueChange = { berat = it; isError = false }, label = { Text("Berat (Kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = isError, modifier = Modifier.fillMaxWidth())

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
                    categories.forEach { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = (paket == category.name), onClick = { paket = category.name })
                            Text(category.name)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Foto Bukti Barang:", fontWeight = FontWeight.Bold)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .heightIn(min = 200.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                        .clickable {
                            imageCropLauncher.launch(CropImageContractOptions(null, CropImageOptions(imageSourceIncludeCamera = true, fixAspectRatio = true)))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "Klik untuk Ambil Foto",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (isError) {
                    Text("Peringatan: Nama, Berat, dan Foto harus diisi!", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text("Total: $totalFormatted", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                }

                Button(
                    onClick = {
                        if (user.email.isEmpty()) {
                            Toast.makeText(context, "Silakan login dengan Google terlebih dahulu untuk menyimpan pesanan!", Toast.LENGTH_LONG).show()
                            showProfileDialog = true
                        } else if (namaPelanggan.isNotEmpty() && berat.isNotEmpty() && imageUri != null) {
                            showDialog = true
                            isError = false
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan Pesanan")
                }
            }

            if ((apiState is ApiState.Loading && !isPullRefreshing) || isAuthLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                }
            }

            if (apiState is ApiState.Error) {
                val errorMessage = (apiState as ApiState.Error).message

                if (isPullRefreshing) {
                    LaunchedEffect(apiState) {
                        Toast.makeText(context, "Maaf, internet anda tidak ada \uD83D\uDE22", Toast.LENGTH_SHORT).show()
                        viewModel.clearApiState()
                    }
                } else {
                    val displayMessage = if (errorMessage.contains("failed to connect") || errorMessage.contains("Unable to resolve host") || errorMessage.contains("timeout")) {
                        "Tidak ada koneksi internet atau server tidak dapat dijangkau. Data tetap disimpan secara offline dan dapat dikirim nanti."
                    } else {
                        errorMessage
                    }

                    AlertDialog(
                        onDismissRequest = { viewModel.clearApiState() },
                        title = { Text("Informasi Sistem") },
                        text = { Text(displayMessage) },
                        confirmButton = {
                            Button(onClick = { viewModel.clearApiState() }) {
                                Text("Tutup")
                            }
                        }
                    )
                }
            }

            if (apiState is ApiState.Success) {
                LaunchedEffect(Unit) {
                    if (isPullRefreshing) {
                        Toast.makeText(context, "Koneksi stabil. Sinkronisasi sukses!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Pesanan berhasil disimpan & disinkronisasi!", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.clearApiState()
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Konfirmasi") },
                text = { Text("Simpan pesanan atas nama $namaPelanggan?") },
                confirmButton = {
                    Button(onClick = {
                        val imageFile = imageUri?.let { uri ->
                            val tempFile = java.io.File(context.cacheDir, "upload_image.jpg")
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val outputStream = java.io.FileOutputStream(tempFile)
                            inputStream?.copyTo(outputStream)
                            inputStream?.close()
                            outputStream.close()
                            tempFile
                        }

                        viewModel.insertOrder(
                            nama = namaPelanggan,
                            berat = berat,
                            isJaket = hasJaket,
                            isSprei = hasSprei,
                            paket = paket,
                            total = totalHarga,
                            estimasi = hitungEstimasi(paket),
                            imageUri = imageUri?.toString(),
                            imageFile = imageFile
                        )

                        namaPelanggan = ""
                        berat = ""
                        imageUri = null
                        isError = false
                        showDialog = false
                        navController.navigate(Screen.History.route)
                    }) { Text("Ya") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
            )
        }

        if (showProfileDialog) {
            ProfilDialog(
                user = user,
                isDarkMode = isDarkMode,
                onThemeChange = { viewModel.toggleTheme(!isDarkMode) },
                onDismissRequest = { showProfileDialog = false },
                onLoginClick = {
                    showProfileDialog = false
                    isAuthLoading = true

                    coroutineScope.launch {
                        val loggedInUser = signInWithGoogle(context)
                        if (loggedInUser != null) {
                            userDataStore.saveUserData(loggedInUser)
                            viewModel.fetchOrdersFromApi()
                            Toast.makeText(context, "Selamat datang, ${loggedInUser.name}!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Login dibatalkan atau gagal. Coba lagi.", Toast.LENGTH_SHORT).show()
                        }

                        isAuthLoading = false
                    }
                },
                onLogoutClick = {
                    showProfileDialog = false
                    isAuthLoading = true

                    coroutineScope.launch {
                        delay(1000L)
                        userDataStore.clearUserData()
                        viewModel.clearApiState()
                        isAuthLoading = false
                        Toast.makeText(context, "Berhasil logout.", Toast.LENGTH_SHORT).show()
                    }
                },
                onHistoryClick = {
                    showProfileDialog = false
                    navController.navigate(Screen.History.route)
                },
                onRecycleBinClick = {
                    showProfileDialog = false
                    navController.navigate(Screen.RecycleBin.route)
                },
                onAboutClick = {
                    showProfileDialog = false
                    onAboutClick()
                }
            )
        }
    }
}

suspend fun signInWithGoogle(context: Context): User? {
    val credentialManager = CredentialManager.create(context)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            User(
                name = googleIdTokenCredential.displayName ?: "Pengguna",
                email = googleIdTokenCredential.id,
                photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: ""
            )
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}