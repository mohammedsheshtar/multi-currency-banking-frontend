package com.joincoded.bankapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joincoded.bankapi.ui.theme.BankTheme
import com.joincoded.bankapi.ui.screens.MainScreen
import com.joincoded.bankapi.utils.Constants
import com.joincoded.bankapi.repository.CardRepository
import com.joincoded.bankapi.network.CardApiService
import com.joincoded.bankapi.network.RetrofitClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize repository with base URL
        val cardApiService = RetrofitClient.createService(CardApiService::class.java, Constants.baseUrl)
        val repository = CardRepository(cardApiService)

        setContent {
            BankTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigator(
                        navController = navController,
                        repository = repository
                    )
                }
            }
        }
    }
}

enum class NavRoutes(val value: String) {
    NAV_ROUTE_EXCHANGE_RATE_SCREEN("exchangeRateScreen"),
    NAV_ROUTE_HOME_SCREEN("homeScreen"),
    NAV_ROUTE_LOGIN_SCREEN("loginScreen"),
    NAV_ROUTE_REGISTRATION_SCREEN("registrationScreen"),
    NAV_ROUTE_PROFILE_SCREEN("profileScreen"),
    NAV_ROUTE_SHOP_SCREEN("shopScreen"),
    NAV_ROUTE_SHOP_HISTORY("shopHistory"),
    NAV_ROUTE_CARD_SCREEN("cardScreen")
}

@Composable
fun AppNavigator(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    repository: CardRepository,
    startDestination: String = NavRoutes.NAV_ROUTE_CARD_SCREEN.value
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.NAV_ROUTE_CARD_SCREEN.value) {
            MainScreen(
                navController = navController,
                onNavigateToProfile = {
                    navController.navigate(NavRoutes.NAV_ROUTE_PROFILE_SCREEN.value)
                },
                repository = repository
            )
        }
        // Add other navigation routes here
    }
}
