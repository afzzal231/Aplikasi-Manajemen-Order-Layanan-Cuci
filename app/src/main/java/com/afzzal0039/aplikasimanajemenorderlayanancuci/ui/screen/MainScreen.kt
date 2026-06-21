package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.LaundryViewModel
import com.afzzal0039.aplikasimanajemenorderlayanancuci.util.UserDataStore
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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
    var showProfileDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LaundryAja") },
                actions = {
                    IconButton(onClick = { viewModel.toggleTheme(!isDarkMode) }) {
                        Icon(painter = painterResource(id = if (isDarkMode) R.drawable.baseline_light_mode_24 else R.drawable.baseline_dark_mode_24), contentDescription = null)
                    }
                    IconButton(onClick = { showProfileDialog = true }) {
                        if (user.email.isEmpty()) Icon(Icons.Default.AccountCircle, null)
                        else AsyncImage(model = user.photoUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    .height(150.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = {
                        imageCropLauncher.launch(CropImageContractOptions(null, CropImageOptions(imageSourceIncludeCamera = true, fixAspectRatio = true)))
                    },
                    modifier = Modifier.fillMaxSize(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text("Klik untuk Ambil Foto")
                    }
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
                    if (namaPelanggan.isNotEmpty() && berat.isNotEmpty() && imageUri != null) {
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

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Konfirmasi") },
                text = { Text("Simpan pesanan atas nama $namaPelanggan?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.insertOrder(namaPelanggan, berat, hasJaket, hasSprei, paket, totalHarga, hitungEstimasi(paket), imageUri = imageUri?.toString())
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
                onDismissRequest = { showProfileDialog = false },
                onLoginClick = {
                    showProfileDialog = false
                    coroutineScope.launch {
                        val loggedInUser = signInWithGoogle(context)
                        if (loggedInUser != null) {
                            userDataStore.saveUserData(loggedInUser)
                            viewModel.fetchOrdersFromApi()
                        }
                    }
                },
                onLogoutClick = {
                    coroutineScope.launch {
                        userDataStore.clearUserData()
                        viewModel.clearApiState()
                        showProfileDialog = false
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