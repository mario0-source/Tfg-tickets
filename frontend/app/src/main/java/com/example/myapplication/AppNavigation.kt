package com.example.myapplication



import androidx.compose.runtime.Composable

import androidx.compose.ui.platform.LocalContext

import androidx.navigation.NavType

import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController

import androidx.navigation.navArgument

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



        composable(

            route = "ticketDetail/{ticketId}",

            arguments = listOf(navArgument("ticketId") { type = NavType.IntType })

        ) { backStackEntry ->

            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: 0

            TicketDetailScreen(navController = navController, ticketId = ticketId)

        }



        composable(

            route = "ticketEdit/{ticketId}",

            arguments = listOf(navArgument("ticketId") { type = NavType.IntType })

        ) { backStackEntry ->

            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: 0

            EditTicketScreen(navController = navController, ticketId = ticketId)

        }



        composable("compare") { CompareScreen(navController) }



        composable(

            route = "compareDetail/{productName}",

            arguments = listOf(navArgument("productName") { type = NavType.StringType })

        ) { backStackEntry ->

            val productName = backStackEntry.arguments?.getString("productName") ?: ""

            CompareDetailScreen(navController = navController, encodedProductName = productName)

        }



        composable("profile") { ProfileScreen(navController) }

        composable("register") {

            RegisterScreen(

                onRegisterSuccess = { navController.navigate("login") },

                onNavigateToLogin = { navController.navigate("login") }

            )

        }

    }

}

