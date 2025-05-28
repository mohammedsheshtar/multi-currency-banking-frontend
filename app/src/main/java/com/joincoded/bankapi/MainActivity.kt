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
import androidx.navigation.compose.rememberNavController
import com.joincoded.bankapi.ui.theme.BankAPITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BankAPITheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

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



}

@Composable
fun AppNavigator(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {

    }
}

