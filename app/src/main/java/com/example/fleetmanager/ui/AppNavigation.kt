package com.example.fleetmanager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.fleetmanager.ui.screens.*

data class TabItem(val route: String, val label: String, val icon: ImageVector)

val tabItems = listOf(
    TabItem("home", "首页", Icons.Default.Home),
    TabItem("members", "队员", Icons.Default.Groups),
    TabItem("tasks", "任务", Icons.Default.Assignment),
    TabItem("more", "更多", Icons.Default.Menu)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController, vm: com.example.fleetmanager.viewmodel.AppViewModel) {
    val currentUser by vm.currentUser.collectAsState()

    if (currentUser == null) {
        LoginScreen(vm = vm)
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentDestination = navController.currentBackStackEntryAsState().value?.destination
                tabItems.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController, vm) }
            composable("members") { MemberListScreen(navController, vm) }
            composable(
                "member_edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                MemberEditScreen(navController, vm, backStack.arguments!!.getLong("id"))
            }
            composable("tasks") { TaskListScreen(navController, vm) }
            composable(
                "task_edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                TaskEditScreen(navController, vm, backStack.arguments!!.getLong("id"))
            }
            composable("activities") { ActivityListScreen(navController, vm) }
            composable(
                "checkin/{activityId}",
                arguments = listOf(navArgument("activityId") { type = NavType.LongType })
            ) { backStack ->
                CheckInScreen(navController, vm, backStack.arguments!!.getLong("activityId"))
            }
            composable("attendance") { AttendanceScreen(navController, vm) }
            composable("announcements") { AnnouncementListScreen(navController, vm) }
            composable(
                "announcement_edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                AnnouncementEditScreen(navController, vm, backStack.arguments!!.getLong("id"))
            }
            composable(
                "announcement_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                AnnouncementDetailScreen(navController, vm, backStack.arguments!!.getLong("id"))
            }
            composable("items") { ItemListScreen(navController, vm) }
            composable("borrows") { BorrowScreen(navController, vm) }
            composable("calendar") { CalendarScreen(navController, vm) }
            composable("more") { MoreScreen(navController, vm) }
            composable("profile") { ProfileScreen(navController, vm) }
        }
    }
}
