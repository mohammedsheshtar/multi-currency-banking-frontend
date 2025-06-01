package com.joincoded.bankapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.multicurrency_card.SVG.BankCardsIcon
import com.example.multicurrency_card.SVG.ShopFilledIcon
import com.joincoded.bankapi.NavBar.NavBarItem
import com.joincoded.bankapi.NavBar.WaveBottomNavBar
import com.joincoded.bankapi.SVG.CurrencyExchangeIcon
import com.joincoded.bankapi.SVG.Home3FillIcon
import com.joincoded.bankapi.SVG.UserSolidIcon

// ✅ Make sure this is correct
import com.joincoded.bankapi.ViewModel.WalletViewModel
import com.joincoded.bankapi.composable.FanCarouselView
import com.joincoded.bankapi.data.CardState
import com.joincoded.bankapi.data.PaymentCard
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.profile.ProfileScreen
import com.joincoded.bankapi.ui.theme.BankAPITheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitHelper.initialize(applicationContext)
        setContent {
            BankAPITheme {
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

enum class NavRoutes(val value: String) {
    NAV_ROUTE_WALLET_SCREEN("WalletScreen")
}

@Composable
fun AppNavigator(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.NAV_ROUTE_WALLET_SCREEN.value
) {
    val context = LocalContext.current
    val walletViewModel: WalletViewModel = viewModel<WalletViewModel>()
    var currentIndex by remember { mutableStateOf(2) } // default to BankCards tab
    val cards by walletViewModel.cards.collectAsState()
    val error by walletViewModel.error.collectAsState()


    // Init + Login + Fetch cards
    LaunchedEffect(Unit) {
        walletViewModel.initialize(context)
        walletViewModel.loginAndLoadWallet(context)
    }

    val navItems = listOf(
        NavBarItem("Home", { isSelected -> Home3FillIcon(color = if (isSelected) Color(0xFFA086CE) else Color.Gray) }, Color.White),
        NavBarItem("CurrencyExchange", { isSelected -> CurrencyExchangeIcon(color = if (isSelected) Color(0xFFA086CE) else Color.Gray) }, Color.White),
        NavBarItem("BankCards", { isSelected -> BankCardsIcon(color = if (isSelected) Color(0xFFA086CE) else Color.Gray) }, Color.White),
        NavBarItem("ShopFilled", { isSelected -> ShopFilledIcon(color = if (isSelected) Color(0xFFA086CE) else Color.Gray) }, Color.White),
        NavBarItem("Profile", { isSelected -> UserSolidIcon(color = if (isSelected) Color(0xFFA086CE) else Color.Gray) }, Color.White)
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.NAV_ROUTE_WALLET_SCREEN.value) {
            Scaffold(
                bottomBar = {
                    WaveBottomNavBar(
                        items = navItems,
                        currentIndex = currentIndex,
                        onItemSelected = { index -> currentIndex = index }
                    )
                }
            ) { padding ->
                when (navItems[currentIndex].label) {
                    "Home" -> Text("Home Placeholder")
                    "CurrencyExchange" -> Text("Currency Exchange Placeholder")
                    "BankCards" -> {
                        LaunchedEffect(Unit) {
                            android.util.Log.d("AppNavigator", "📦 Forcing manual fetchUserCards()")
                            walletViewModel.fetchUserCards()
                        }

                        if (error != null) {
                            Text(
                                text = "❌ ${error ?: "Unknown error"}",
                                color = Color.Red,
                                modifier = Modifier.padding(padding).fillMaxSize()
                            )
                        } else if (cards.isEmpty()) {
                            Text(
                                text = "No accounts found or still loading...",
                                color = Color.White,
                                modifier = Modifier.padding(padding).fillMaxSize()
                            )
                        } else {
                            FanCarouselView(
                                cards = cards.map { it.card },
                                modifier = Modifier.padding(padding)
                            )
                        }
                    }

                    "ShopFilled" -> Text("Shop Placeholder")
                    "Profile" -> ProfileScreen()
                }
            }
        }
    }
}
