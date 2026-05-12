package com.afzzal0039.aplikasimanajemenorderlayanancuci.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.LaundryViewModel
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.EditScreen
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.HistoryScreen
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.MainScreen
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.Screen
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.AboutScreen
import com.afzzal0039.aplikasimanajemenorderlayanancuci.ui.screen.RecycleBinScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: LaundryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {

        composable(Screen.Main.route) {
            MainScreen(
                navController = navController,
                viewModel = viewModel,
                onAboutClick = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.RecycleBin.route) {
            RecycleBinScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: -1
            EditScreen(
                orderId = orderId,
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}