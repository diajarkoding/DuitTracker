package com.diajarkoding.duittracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diajarkoding.duittracker.ui.features.auth.LoginScreen
import com.diajarkoding.duittracker.ui.features.auth.RegisterScreen
import com.diajarkoding.duittracker.ui.features.categorytransactions.CategoryTransactionsScreen
import com.diajarkoding.duittracker.ui.features.dashboard.DashboardScreen
import com.diajarkoding.duittracker.ui.features.detail.TransactionDetailScreen
import com.diajarkoding.duittracker.ui.features.edit.EditTransactionScreen
import com.diajarkoding.duittracker.ui.features.input.AddTransactionScreen
import com.diajarkoding.duittracker.ui.features.language.LanguageScreen
import com.diajarkoding.duittracker.ui.features.profile.ProfileScreen
import com.diajarkoding.duittracker.ui.features.reminder.ReminderScreen
import com.diajarkoding.duittracker.ui.features.splash.SplashScreen
import com.diajarkoding.duittracker.ui.features.statistics.StatisticsScreen
import com.diajarkoding.duittracker.ui.features.theme.ThemeScreen
import com.diajarkoding.duittracker.ui.theme.NeoTheme

@Composable
fun DuitTrackerNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    // rememberNavController() will preserve navigation state across recompositions
    // State to signal dashboard refresh after transaction changes
    var shouldRefreshDashboard by rememberSaveable { mutableStateOf(false) }
    
    // Background color for transitions - prevents gray blink in dark mode
    val backgroundColor = NeoTheme.colors.background
    
    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
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
                onNavigateToLanguage = {
                    navController.navigate(Routes.Language)
                },
                onNavigateToTheme = {
                    navController.navigate(Routes.Theme)
                },
                onNavigateToReminder = {
                    navController.navigate(Routes.Reminder)
                },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Dashboard) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Language> {
            LanguageScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Theme> {
            ThemeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.Reminder> {
            ReminderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
