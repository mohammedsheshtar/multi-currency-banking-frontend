package com.joincoded.bankapi

import ProfileScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joincoded.bankapi.composable.ExchangeRateScreen
import com.joincoded.bankapi.composable.LoginScreen
import com.joincoded.bankapi.composable.RegistrationScreen
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.viewmodel.AuthViewModel

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


    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value) {
            val authViewModel: AuthViewModel = viewModel()
            val token by authViewModel.token.collectAsState()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

            LoginScreen(
                navController = navController,
                onRegisterClick = {
                    navController.navigate(NavRoutes.NAV_ROUTE_REGISTRATION_SCREEN.value)
                },
                onForgotPasswordClick = { println("Forgot password clicked") },
                onSocialClick = { platform -> println("Social clicked: $platform") }
            )

            LaunchedEffect(isLoggedIn, token) {
                if (isLoggedIn && token != null) {
                    navController.navigate("${NavRoutes.NAV_ROUTE_PROFILE_SCREEN.value}/$token") {
                        popUpTo(NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value) { inclusive = true }
                    }
                }
            }
        }


        composable(NavRoutes.NAV_ROUTE_REGISTRATION_SCREEN.value) {
            RegistrationScreen(navController = navController)
        }

        composable("${NavRoutes.NAV_ROUTE_PROFILE_SCREEN.value}/{token}") { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            ProfileScreen(
                token = token,
                onLogout = {
                    navController.navigate(NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value) {
                        popUpTo(NavRoutes.NAV_ROUTE_PROFILE_SCREEN.value) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.NAV_ROUTE_LOGIN_SCREEN.value + "?registered={registered}") { backStackEntry ->
            val registered = backStackEntry.arguments?.getString("registered") == "true"
            LoginScreen(
                navController = navController,
                registered = registered,
                onRegisterClick = { navController.navigate(NavRoutes.NAV_ROUTE_REGISTRATION_SCREEN.value) },
                onForgotPasswordClick = { /* TODO: Add forgot password behavior */ },
                onSocialClick = { /* TODO: Handle social login */ }
            )

        }

    }
}