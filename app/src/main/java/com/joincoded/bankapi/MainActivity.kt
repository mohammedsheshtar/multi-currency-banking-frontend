package com.joincoded.bankapi

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.joincoded.bankapi.composable.*
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.ui.theme.MultiCurrencyBankingTheme
import com.joincoded.bankapi.viewmodel.*
import com.joincoded.bankapi.NavBar.WaveBottomNavBar
import com.joincoded.bankapi.NavBar.NavBarItem
import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.example.multicurrency_card.SVG.*
import com.joincoded.bankapi.SVG.*
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure window insets
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            MultiCurrencyBankingTheme {
                val view = LocalView.current
                val insetsController = remember { WindowCompat.getInsetsController(window, view) }
                
                // Handle system bars
                DisposableEffect(Unit) {
                    insetsController.apply {
                        isAppearanceLightStatusBars = false
                        isAppearanceLightNavigationBars = false
                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                    onDispose {}
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            authViewModel = authViewModel,
            context = LocalContext.current
        )
    )
    val shopViewModel: ShopViewModel = viewModel()
    val shopHistoryViewModel: ShopHistoryViewModel = viewModel()
    val context = LocalContext.current
    val walletViewModel: WalletViewModel = viewModel(
        factory = WalletViewModelFactory(context.applicationContext as Application)
    )
    val kycViewModel: KycViewModel = viewModel()

    // Initialize AuthViewModel with context
    LaunchedEffect(Unit) {
        authViewModel.initialize(context)
    }

    // Collect token state in a composable context
    val tokenState by authViewModel.token.collectAsState()
    val currentToken = tokenState ?: ""

    // Track current navigation item
    var currentNavIndex by remember { mutableStateOf(0) }

    // Define navigation items
    val navItems = listOf(
        NavBarItem("Home", { isSelected -> Home3FillIcon(modifier = Modifier.size(30.dp), color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)) }),
        NavBarItem("ExchangeRate", { isSelected -> CurrencyExchangeIcon(modifier = Modifier.size(30.dp), color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)) }),
        NavBarItem("Wallet", { isSelected -> BankCardsIcon(modifier = Modifier.size(30.dp), color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)) }),
        NavBarItem("Shop", { isSelected -> ShopFilledIcon(modifier = Modifier.size(30.dp), color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)) }),
        NavBarItem("Profile", { isSelected -> UserSolidIcon(modifier = Modifier.size(30.dp), color = if (isSelected) Color(0xFFA086CE) else Color(0xFF9E9E9E)) })
    )

    Scaffold(
        bottomBar = {
            WaveBottomNavBar(
                items = navItems,
                currentIndex = currentNavIndex,
                onItemSelected = { index ->
                    currentNavIndex = index
                    when (index) {
                        0 -> navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                        1 -> navController.navigate("exchange_rate") {
                            popUpTo("exchange_rate") { inclusive = true }
                            launchSingleTop = true
                        }
                        2 -> navController.navigate("wallet") {
                            popUpTo("wallet") { inclusive = true }
                            launchSingleTop = true
                        }
                        3 -> navController.navigate("shop") {
                            popUpTo("shop") { inclusive = true }
                            launchSingleTop = true
                        }
                        4 -> navController.navigate("profile") {
                            popUpTo("profile") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
                bottom = 8.dp
            )
        ) {
            composable(
                route = "login",
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                LoginScreen(
                    navController = navController,
                    onLoginSuccess = { navController.navigate("home") },
                    onRegisterClick = { navController.navigate("register") },
                    onForgotPasswordClick = { navController.navigate("forgot_password") },
                    onSocialClick = { /* Handle social login */ },
                    context = context
                )
            }

            composable(
                route = "register",
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                RegistrationScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    onLoginClick = { navController.navigate("login") },
                    context = context
                )
            }

            composable(
                route = "home",
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                if (currentToken.isNotBlank()) {
                    HomeScreen(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        shopViewModel = shopViewModel,
                        walletViewModel = walletViewModel
                    )
                } else {
                    LoginScreen(
                        navController = navController,
                        onLoginSuccess = { navController.navigate("home") },
                        onRegisterClick = { navController.navigate("register") },
                        onForgotPasswordClick = { navController.navigate("forgot_password") },
                        onSocialClick = { /* Handle social login */ },
                        context = context
                    )
                }
            }

            composable(
                route = "exchange_rate",
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                if (currentToken.isNotBlank()) {
                    ExchangeRateScreen(
                        navController = navController,
                        walletViewModel = walletViewModel
                    )
                } else {
                    LoginScreen(
                        navController = navController,
                        onLoginSuccess = { navController.navigate("home") },
                        onRegisterClick = { navController.navigate("register") },
                        onForgotPasswordClick = { navController.navigate("forgot_password") },
                        onSocialClick = { /* Handle social login */ },
                        context = context
                    )
                }
            }

            composable(
                route = "wallet",
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                if (currentToken.isNotBlank()) {
                    WalletScreen(
                        navController = navController,
                        walletViewModel = walletViewModel
                    )
                } else {
                    LoginScreen(
                        navController = navController,
                        onLoginSuccess = { navController.navigate("home") },
                        onRegisterClick = { navController.navigate("register") },
                        onForgotPasswordClick = { navController.navigate("forgot_password") },
                        onSocialClick = { /* Handle social login */ },
                        context = context
                    )
                }
            }

            composable(
                route = "shop",
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                if (currentToken.isNotBlank()) {
                    ShopScreen(
                        navController = navController,
                        shopViewModel = shopViewModel,
                        token = currentToken
                    )
                } else {
                    LoginScreen(
                        navController = navController,
                        onLoginSuccess = { navController.navigate("home") },
                        onRegisterClick = { navController.navigate("register") },
                        onForgotPasswordClick = { navController.navigate("forgot_password") },
                        onSocialClick = { /* Handle social login */ },
                        context = context
                    )
                }
            }

            composable(
                route = "profile",
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                if (currentToken.isNotBlank()) {
                    ProfileScreen(
                        navController = navController,
                        viewModel = kycViewModel,
                        onLogout = {
                            authViewModel.logout(context)
                            navController.navigate("login") {
                                popUpTo("profile") { inclusive = true }
                            }
                        }
                    )
                } else {
                    LoginScreen(
                        navController = navController,
                        onLoginSuccess = { navController.navigate("home") },
                        onRegisterClick = { navController.navigate("register") },
                        onForgotPasswordClick = { navController.navigate("forgot_password") },
                        onSocialClick = { /* Handle social login */ },
                        context = context
                    )
                }
            }

            composable(
                route = "shop_history",
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                if (currentToken.isNotBlank()) {
                    ShopHistoryScreen(
                        viewModel = shopHistoryViewModel,
                        token = currentToken
                    )
                } else {
                    LoginScreen(
                        navController = navController,
                        onLoginSuccess = { navController.navigate("home") },
                        onRegisterClick = { navController.navigate("register") },
                        onForgotPasswordClick = { navController.navigate("forgot_password") },
                        onSocialClick = { /* Handle social login */ },
                        context = context
                    )
                }
            }
        }
    }
}
