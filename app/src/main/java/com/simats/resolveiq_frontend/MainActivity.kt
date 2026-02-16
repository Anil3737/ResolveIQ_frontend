package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.resolveiq_frontend.api.RetrofitClient
import com.simats.resolveiq_frontend.repository.AuthRepository
import com.simats.resolveiq_frontend.ui.Screen
import com.simats.resolveiq_frontend.ui.screens.*
import com.simats.resolveiq_frontend.utils.TokenManager
import com.simats.resolveiq_frontend.viewmodel.AuthViewModel
import com.simats.resolveiq_frontend.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val tokenManager = TokenManager(this)
        val api = RetrofitClient.getApi(this)
        val authRepository = AuthRepository(api)

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val context = this@MainActivity
                    
                    NavHost(navController = navController, startDestination = Screen.Splash.route) {
                        composable(Screen.Splash.route) {
                            SplashScreen(navController, tokenManager)
                        }
                        composable(Screen.Login.route) {
                            val viewModel: AuthViewModel = viewModel(
                                factory = ViewModelFactory(authRepository, tokenManager)
                            )
                            LoginScreen(navController, viewModel)
                        }
                        composable(Screen.Register.route) {
                            RegisterScreen(navController, authRepository)
                        }
                        composable(Screen.EmployeeDashboard.route) {
                            EmployeeDashboardScreen(navController, context)
                        }
                        composable(Screen.CreateTicket.route) {
                            CreateTicketScreen(navController, context)
                        }
                    }
                }
            }
        }
    }
}