package com.joincoded.bankapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joincoded.bankapi.composable.ExchangeRateScreen
import com.joincoded.bankapi.login.LoginScreen
import com.joincoded.bankapi.login.RegistrationScreen
import com.joincoded.bankapi.profile.ProfileScreen
import com.joincoded.bankapi.ui.theme.BankAPITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BankAPITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigator()
                    }
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
    NAV_ROUTE_SHOP_HISTORY("shopHistory")
}

@Composable
fun AppNavigator(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value) {
            LoginScreen(
                navController = navController,
                onRegisterClick = {
                    navController.navigate(NavRoutes.NAV_ROUTE_REGISTRATION_SCREEN.value)
                },
                onForgotPasswordClick = {
                    println("Forgot password clicked")
                },
                onSocialClick = { platform ->
                    println("Social clicked: $platform")
                }
            )
        }

        composable(NavRoutes.NAV_ROUTE_REGISTRATION_SCREEN.value) {
            RegistrationScreen(navController = navController)
        }

        composable(NavRoutes.NAV_ROUTE_PROFILE_SCREEN.value) {
            ProfileScreen()
        }
    }
}

