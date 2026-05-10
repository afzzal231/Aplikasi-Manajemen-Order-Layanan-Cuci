package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

sealed class Screen(val route: String) {
    object Main : Screen("Main")
    object About : Screen("About")

    object Result : Screen("Result/{berat}/{paket}/{hasJaket}/{hasSprei}") {
        fun createRoute(berat: String, paket: String, hasJaket: Boolean, hasSprei: Boolean): String {
            return "Result/$berat/$paket/$hasJaket/$hasSprei"
        }
    }
}