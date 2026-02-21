package com.studentbudget.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studentbudget.ui.screens.AnalyticsScreen
import com.studentbudget.ui.screens.BudgetScreen
import com.studentbudget.ui.screens.CalendarScreen
import com.studentbudget.ui.screens.DashboardScreen
import com.studentbudget.ui.screens.ExpenseEntryDialog
import com.studentbudget.ui.screens.HistoryScreen
import com.studentbudget.ui.screens.ProfileScreen
import com.studentbudget.ui.theme.Black
import com.studentbudget.ui.theme.DarkGrey
import com.studentbudget.ui.theme.MidGrey
import com.studentbudget.ui.theme.White
import com.studentbudget.viewmodel.BudgetViewModel
import com.studentbudget.data.Expense

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    data object Calendar  : Screen("calendar", "Calendar", Icons.Default.DateRange)
    data object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    data object Budget    : Screen("budget", "Budget", Icons.Default.CreditCard)
    data object History   : Screen("history", "History", Icons.Default.History)
    data object Profile   : Screen("profile", "Profile", Icons.Default.Person)
}

val screens = listOf(Screen.Dashboard, Screen.Calendar, Screen.Analytics, Screen.Budget, Screen.History, Screen.Profile)

@Composable
fun StudentBudgetApp(viewModel: BudgetViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showExpenseDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    val pinCode by viewModel.pinCode.collectAsState()
    var isUnlocked by remember { mutableStateOf(false) }

    if (pinCode.isNotBlank() && !isUnlocked) {
        com.studentbudget.ui.screens.PinLockScreen(
            correctPin = pinCode,
            onUnlocked = { isUnlocked = true }
        )
    } else {
        Box {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showExpenseDialog = true },
                        shape = CircleShape,
                        containerColor = White,
                        contentColor = Black,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    }
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkGrey,
                        tonalElevation = 0.dp,
                    ) {
                        screens.forEach { screen ->
                            NavigationBarItem(
                                selected = currentRoute == screen.route,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        screen.icon,
                                        contentDescription = screen.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        screen.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = White,
                                    selectedTextColor = White,
                                    unselectedIconColor = MidGrey,
                                    unselectedTextColor = MidGrey,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(viewModel = viewModel, onSeeAllHistory = {
                                navController.navigate(Screen.History.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            })
                        }
                        composable(Screen.Calendar.route) {
                            CalendarScreen(viewModel = viewModel)
                        }
                        composable(Screen.Analytics.route) {
                            AnalyticsScreen(viewModel = viewModel)
                        }
                        composable(Screen.Budget.route) {
                            BudgetScreen(viewModel = viewModel)
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(
                                viewModel = viewModel,
                                onEditExpense = { expense ->
                                    editingExpense = expense
                                    showExpenseDialog = true
                                }
                            )
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }

            // Full-screen overlay — OUTSIDE the Scaffold so it covers everything
            if (showExpenseDialog) {
                ExpenseEntryDialog(
                    viewModel = viewModel,
                    onDismiss = {
                        showExpenseDialog = false
                        editingExpense = null
                    },
                    editExpense = editingExpense
                )
            }
        }
    }
}
