package com.diajarkoding.duittracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.diajarkoding.duittracker.ui.features.auth.LoginScreen
import com.diajarkoding.duittracker.ui.features.auth.RegisterScreen
import com.diajarkoding.duittracker.ui.features.categorytransactions.CategoryTransactionsScreen
import com.diajarkoding.duittracker.ui.features.dashboard.DashboardScreen
import com.diajarkoding.duittracker.ui.features.detail.TransactionDetailScreen
import com.diajarkoding.duittracker.ui.features.edit.EditTransactionScreen
import com.diajarkoding.duittracker.ui.features.input.AddTransactionScreen
import com.diajarkoding.duittracker.ui.features.profile.ProfileScreen
import com.diajarkoding.duittracker.ui.features.splash.SplashScreen
import com.diajarkoding.duittracker.ui.features.statistics.StatisticsScreen

@Composable
fun DuitTrackerNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // State to signal dashboard refresh after transaction changes
    var shouldRefreshDashboard by rememberSaveable { mutableStateOf(false) }
    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
        modifier = modifier
    ) {
        composable<Routes.Splash> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.Register)
                }
            )
        }

        composable<Routes.Register> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Dashboard> {
            DashboardScreen(
                onAddClick = {
                    navController.navigate(Routes.AddTransaction)
                },
                onTransactionClick = { transactionId ->
                    navController.navigate(Routes.TransactionDetail(transactionId))
                },
                onProfileClick = {
                    navController.navigate(Routes.Profile)
                },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Dashboard) { inclusive = true }
                    }
                },
                shouldRefresh = shouldRefreshDashboard,
                onRefreshHandled = { shouldRefreshDashboard = false }
            )
        }

        composable<Routes.AddTransaction> {
            AddTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTransactionAdded = {
                    shouldRefreshDashboard = true
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.TransactionDetail> {
            TransactionDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditClick = { transactionId ->
                    navController.navigate(Routes.EditTransaction(transactionId))
                },
                onTransactionDeleted = {
                    shouldRefreshDashboard = true
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.EditTransaction> {
            EditTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTransactionUpdated = {
                    shouldRefreshDashboard = true
                    // Pop back to dashboard (skipping detail screen)
                    navController.popBackStack(Routes.Dashboard, inclusive = false)
                }
            )
        }

        composable<Routes.Statistics> {
            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCategoryClick = { category, year, month, isExpense ->
                    navController.navigate(Routes.CategoryTransactions(category, year, month, isExpense))
                }
            )
        }

        composable<Routes.CategoryTransactions> {
            CategoryTransactionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTransactionClick = { transactionId ->
                    navController.navigate(Routes.TransactionDetail(transactionId))
                }
            )
        }

        composable<Routes.Profile> {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToStatistics = {
                    navController.navigate(Routes.Statistics)
                },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Dashboard) { inclusive = true }
                    }
                }
            )
        }
    }
}
