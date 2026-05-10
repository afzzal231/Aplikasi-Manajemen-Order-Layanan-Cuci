package com.afzzal0039.aplikasimanajemenorderlayanancuci

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavGraph
import androidx.navigation.compose.rememberNavController
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.theme.AplikasiManajemenOrderLayananCuciTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AplikasiManajemenOrderLayananCuciTheme{
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}