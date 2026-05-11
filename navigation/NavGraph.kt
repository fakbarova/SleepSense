package com.circadianx.sleepsense.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.circadianx.sleepsense.ui.screens.ChallengesScreen
import com.circadianx.sleepsense.ui.screens.ChatScreen
import com.circadianx.sleepsense.ui.screens.DashboardScreen
import com.circadianx.sleepsense.ui.screens.HabitsScreen
import com.circadianx.sleepsense.ui.screens.HistoryScreen
import com.circadianx.sleepsense.ui.screens.AuthScreen
import com.circadianx.sleepsense.ui.screens.OnboardingScreen
import com.circadianx.sleepsense.ui.screens.WalkPlannerScreen
import com.circadianx.sleepsense.ui.screens.ProgressPhotosScreen
import com.circadianx.sleepsense.ui.screens.RecordingScreen
import com.circadianx.sleepsense.ui.screens.ReportScreen
import com.circadianx.sleepsense.ui.screens.SettingsScreen
import com.circadianx.sleepsense.ui.screens.SocialScreen
import com.circadianx.sleepsense.ui.screens.StepsScreen
import com.circadianx.sleepsense.ui.screens.SpotifyScreen
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.viewmodel.DashboardViewModel

sealed class Screen(val route: String) {
    data object Auth        : Screen("auth")
    data object Onboarding  : Screen("onboarding")
    data object Dashboard   : Screen("dashboard")
    data object History     : Screen("history")
    data object Habits      : Screen("habits")
    data object Challenges  : Screen("challenges")
    data object Progress    : Screen("progress")
    data object Steps       : Screen("steps")
    data object WalkPlanner : Screen("walk_planner")
    data object Spotify     : Screen("spotify")
    data object Social      : Screen("social")
    data object Chat        : Screen("chat")
    data object Recording   : Screen("recording")
    data object Report      : Screen("report")
    data object Settings    : Screen("settings")
}

private data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard,  Icons.Filled.Home,         "Home"),
    BottomNavItem(Screen.History,    Icons.Filled.BarChart,     "Sleep"),
    BottomNavItem(Screen.Habits,     Icons.Filled.CheckCircle,  "Habits"),
    BottomNavItem(Screen.Social,     Icons.Filled.People,       "Social"),
    BottomNavItem(Screen.Settings,   Icons.Filled.Person,       "Profile"),
)

private val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()
private const val primaryRootRoute = "dashboard"

@Composable
fun SleepSenseNavGraph() {
    val navController = rememberNavController()
    val colors        = SleepSenseTheme.colors

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavRoutes || currentRoute == Screen.Challenges.route

    Scaffold(
        containerColor = colors.bgDeep,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor  = colors.bgBase,
                    tonalElevation  = androidx.compose.ui.unit.Dp.Unspecified,
                    modifier        = Modifier.navigationBarsPadding()
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy
                            ?.any { it.route == item.screen.route } == true
                            || (item.screen.route == Screen.Habits.route && currentRoute == Screen.Challenges.route)

                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(primaryRootRoute) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector        = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text       = item.label,
                                    fontFamily = JetBrainsMono,
                                    fontSize   = 10.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = colors.purple,
                                selectedTextColor   = colors.purple,
                                indicatorColor      = colors.purple.copy(alpha = 0.12f),
                                unselectedIconColor = colors.textMuted,
                                unselectedTextColor = colors.textMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Onboarding.route,
            modifier         = Modifier.padding(innerPadding),
            enterTransition  = { fadeIn(initialAlpha = 0f) + slideInVertically { it / 30 } },
            exitTransition   = { fadeOut() },
            popEnterTransition  = { fadeIn() },
            popExitTransition   = { fadeOut() + slideOutVertically { it / 30 } }
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    onSignedIn = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    onStartRecording = {
                        navController.navigate(Screen.Recording.route)
                    },
                    onOpenChat = {
                        navController.navigate(Screen.Chat.route)
                    },
                    onOpenReport = {
                        navController.navigate(Screen.Report.route)
                    },
                    viewModel = dashboardViewModel
                )
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Habits.route) {
                HabitsScreen(
                    onOpenChallenges = {
                        navController.navigate(Screen.Challenges.route)
                    }
                )
            }
            composable(Screen.Challenges.route) {
                ChallengesScreen()
            }
            composable(Screen.Progress.route) {
                ProgressPhotosScreen()
            }
            composable(Screen.Steps.route) {
                StepsScreen()
            }
            composable(Screen.WalkPlanner.route) {
                WalkPlannerScreen(
                    stepsRemaining = 0,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Social.route) {
                SocialScreen()
            }
            composable(Screen.Chat.route) {
                ChatScreen()
            }
            composable(Screen.Report.route) {
                ReportScreen()
            }
            composable(Screen.Recording.route) {
                val dashboardEntry = remember(navController) {
                    navController.getBackStackEntry(Screen.Dashboard.route)
                }
                val dashboardViewModel: DashboardViewModel = hiltViewModel(dashboardEntry)
                RecordingScreen(
                    onStop = { navController.popBackStack() },
                    viewModel = dashboardViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onOpenSteps    = { navController.navigate(Screen.Steps.route) },
                    onOpenProgress = { navController.navigate(Screen.Progress.route) },
                    onOpenChat     = { navController.navigate(Screen.Chat.route) },
                    onOpenChallenges = { navController.navigate(Screen.Challenges.route) },
                    onOpenSpotify = { navController.navigate(Screen.Spotify.route) },
                    onSignedOut = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Spotify.route) {
                SpotifyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
