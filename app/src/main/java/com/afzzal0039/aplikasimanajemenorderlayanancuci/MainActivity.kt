package com.afzzal0039.aplikasimanajemenorderlayanancuci

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.afzzal0039.aplikasimanajemenorderlayanancuci.Navigation.NavGraph
import com.afzzal0039.aplikasimanajemenorderlayanancuci.database.OrderDb
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.LaundryViewModel
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.theme.AplikasiManajemenOrderLayananCuciTheme
import com.afzzal0039.aplikasimanajemenorderlayanancuci.util.SettingsDataStore
import com.afzzal0039.aplikasimanajemenorderlayanancuci.util.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = OrderDb.getInstance(applicationContext)
        val dao = database.dao
        val dataStore = SettingsDataStore(applicationContext)
        val factory = ViewModelFactory(dao, dataStore)
        val viewModel: LaundryViewModel = ViewModelProvider(this, factory)[LaundryViewModel::class.java]

        setContent {
            AplikasiManajemenOrderLayananCuciTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController, viewModel = viewModel)
            }
        }
    }
}