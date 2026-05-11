package com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen

sealed class Screen(val route: String) {
    object Main : Screen("main_screen")

    object History : Screen("history_screen")

    object About : Screen("about_screen")

    object RecycleBin : Screen("recycle_bin_screen")

    object Edit : Screen("edit_screen/{orderId}") {
        fun createRoute(orderId: Int) = "edit_screen/$orderId"
    }
}