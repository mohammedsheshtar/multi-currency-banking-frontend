package com.joincoded.bankapi

import android.os.Bundle
import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joincoded.bankapi.composable.HomePage
import com.joincoded.bankapi.composable.ShopPage
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.viewmodel.BankViewModel

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
                        startDestination = NavRoutes.NAV_ROUTE_SHOP_SCREEN.value // change this to SHOP if needed
                    ) {
                        composable(NavRoutes.NAV_ROUTE_HOME_SCREEN.value) {
                            HomePage(navController = navController)
                        }
                        composable(NavRoutes.NAV_ROUTE_SHOP_SCREEN.value) {
                            val bankViewModel: BankViewModel = viewModel()
                            val token = bankViewModel.token ?: ""

                            ShopPage(navController = navController, token = token)
                        }
                        // Add more composable screens here when needed
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



