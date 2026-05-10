package com.example.myapplication


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {

            DigitalNebulaLoginScreen(

                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },

                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("home") {
            HomeDashboardScreen(navController)
        }

        composable("tickets") {
            TicketsScreen(navController)
        }

        composable("add") {
            AddTicketScreen(navController)
        }


        composable("compare") {
            DashboardScreen(navController)
        }

        composable("profile") {
            ProfileScreen(navController)
        }
        composable("register") {

            RegisterScreen(

                onRegisterSuccess = {
                    navController.navigate("login")
                },

                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

    }
}
