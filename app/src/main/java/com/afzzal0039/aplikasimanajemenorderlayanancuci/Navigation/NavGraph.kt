package com.afzzal0039.aplikasimanajemenorderlayanancuci.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.MainScreen
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.ResultScreen
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.Screen


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onCalculate = { berat, paket, hasJaket, hasSprei ->
                    navController.navigate(
                        Screen.Result.createRoute(berat, paket, hasJaket, hasSprei)
                    )
                },
                onAboutClick = { navController.navigate(Screen.About.route) }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("berat") { type = NavType.StringType },
                navArgument("paket") { type = NavType.StringType },
                navArgument("hasJaket") { type = NavType.BoolType },
                navArgument("hasSprei") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val berat = backStackEntry.arguments?.getString("berat") ?: "0"
            val paket = backStackEntry.arguments?.getString("paket") ?: "Reguler"
            val hasJaket = backStackEntry.arguments?.getBoolean("hasJaket") ?: false
            val hasSprei = backStackEntry.arguments?.getBoolean("hasSprei") ?: false

            ResultScreen(
                berat = berat,
                paket = paket,
                hasJaket = hasJaket,
                hasSprei = hasSprei,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}