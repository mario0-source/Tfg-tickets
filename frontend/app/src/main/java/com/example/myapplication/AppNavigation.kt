package com.example.myapplication


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.auth.SessionManager
import com.example.myapplication.network.RetrofitClient

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val token = sessionManager.getToken()
    RetrofitClient.setToken(token)

    val startDestination = if (token.isNullOrEmpty()) "login" else "home"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("login") {
            DigitalNebulaLoginScreen(
                onLoginSuccess = { token ->
                    sessionManager.saveToken(token)
                    RetrofitClient.setToken(token)

                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("home") { HomeDashboardScreen(navController) }
        composable("tickets") { TicketsScreen(navController) }
        composable("add") { AddTicketScreen(navController) }
        composable("ticketDetail") {
            TicketDetailScreen()
        }
        composable("compare") { DashboardScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("register") { RegisterScreen(
            onRegisterSuccess = { navController.navigate("login") },
            onNavigateToLogin = { navController.navigate("login") }
        ) }
    }
}
