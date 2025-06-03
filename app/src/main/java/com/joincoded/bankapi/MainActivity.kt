package com.joincoded.bankapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joincoded.bankapi.composable.HomePage
import com.joincoded.bankapi.composable.ShopHistoryScreen
import com.joincoded.bankapi.composable.ShopPage
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.viewmodel.ShopHistoryViewModel
import com.joincoded.bankapi.viewmodel.HomeViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BankAPITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.NAV_ROUTE_HOME_SCREEN.value
                    ) {
                        composable(NavRoutes.NAV_ROUTE_HOME_SCREEN.value) {
                            HomePage(navController = navController)
                        }
                        composable(NavRoutes.NAV_ROUTE_SHOP_SCREEN.value) {
                            val homeViewModel: HomeViewModel = viewModel()
                            val token = homeViewModel.token

                            if (!token.isNullOrBlank()) {
                                ShopPage(navController = navController, token = token)
                            } else {
                                androidx.compose.material3.Text("Loading...")
                            }
                        }

                        composable(NavRoutes.NAV_ROUTE_SHOP_HISTORY.value) {
                            val homeViewModel: HomeViewModel = viewModel()
                            val shopHistoryViewModel: ShopHistoryViewModel = viewModel()
                            val token = homeViewModel.token ?: ""

                            ShopHistoryScreen(viewModel = shopHistoryViewModel, token = token)
                        }
                    }

                }
            }
        }
    }
}

enum class NavRoutes(val value: String) {
    NAV_ROUTE_LOGIN_SCREEN("loginScreen"),
    NAV_ROUTE_HOME_SCREEN("homeScreen"),
    NAV_ROUTE_EXCHANGE_RATE_SCREEN("exchangeRateScreen"),
    NAV_ROUTE_REGISTRATION_SCREEN("registrationScreen"),
    NAV_ROUTE_PROFILE_SCREEN("profileScreen"),
    NAV_ROUTE_SHOP_SCREEN("shopScreen"),
    NAV_ROUTE_SHOP_HISTORY("shopHistory")
}



