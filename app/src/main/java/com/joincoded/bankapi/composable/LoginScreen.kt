//package com.joincoded.bankapi.composable
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.joincoded.bankapi.viewmodel.BankViewModel
//import com.joincoded.bankapi.NavRoutes
//import androidx.lifecycle.viewmodel.compose.viewModel
//
//
//@Composable
//fun LoginScreen(navController: NavController, viewModel: BankViewModel = viewModel()) {
//    var username by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text("Login", style = MaterialTheme.typography.headlineMedium)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        TextField(
//            value = username,
//            onValueChange = { username = it },
//            label = { Text("Username") },
//            singleLine = true,
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        TextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            singleLine = true,
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                if (username.isNotBlank() && password.isNotBlank()) {
//                    viewModel.signup(username, password)
//                    errorMessage = null
//                } else {
//                    errorMessage = "Please fill both fields"
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Login")
//        }
//
//        if (errorMessage != null) {
//            Text(
//                text = errorMessage ?: "",
//                color = MaterialTheme.colorScheme.error,
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.padding(top = 8.dp)
//            )
//        }
//    }
//
//    // Watch token directly since it's a Compose state
//    LaunchedEffect(viewModel.token) {
//        if (viewModel.token != null) {
//            navController.navigate(NavRoutes.NAV_ROUTE_HOME_SCREEN.value) {
//                popUpTo(0) { inclusive = true }
//            }
//        }
//    }
//}