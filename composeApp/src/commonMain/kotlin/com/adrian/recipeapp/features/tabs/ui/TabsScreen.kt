package com.adrian.recipeapp.features.tabs.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.adrian.recipeapp.features.app.data.Screen
import com.adrian.recipeapp.features.favourites.navigation.favouritesNavGraph
import com.adrian.recipeapp.features.feed.navigation.feedNavGraph
import com.adrian.recipeapp.features.profile.navigation.profileNavGraph
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

val tabItems =
    listOf(
        Screen.Home,
        Screen.Favorites,
        Screen.Profile
    )

@Composable
fun TabsRoute(
    tabNavController: NavHostController,
    navigateToDetail: (Long) -> Unit,
    navigateToSearch: () -> Unit
) {
    TabsScreen(
        tabNavController,
        navigateToDetail,
        navigateToSearch
    )
}

@Composable
fun TabsScreen(
    tabNavController: NavHostController,
    navigateToDetail: (Long) -> Unit,
    navigateToSearch: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.onPrimary
            ) {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                tabItems.forEach { topLevelRoute ->
                    val isSelected =
                        currentDestination?.hierarchy?.any {
                            it.route == topLevelRoute.route
                        } == true
                    NavigationBarItem(
                        colors =
                        NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        ),
                        icon = {
                            val icon =
                                if (isSelected) {
                                    topLevelRoute.selectedIcon
                                } else {
                                    topLevelRoute.unselectedIcon
                                }
                            val color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                }
                            icon?.let {
                                Icon(
                                    tint = color,
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(icon),
                                    contentDescription = topLevelRoute.route
                                )
                            }
                        },
                        label = { Text(stringResource(topLevelRoute.resourceId)) },
                        selected = isSelected,
                        onClick = {
                            tabNavController.navigate(topLevelRoute.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // re-selecting the same item
                                launchSingleTop = true
                                // Restore state when re-selecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            tabNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            feedNavGraph(
                navigateToDetail = navigateToDetail,
                navigateToSearch = navigateToSearch
            )
            favouritesNavGraph(
                navigateToDetail = navigateToDetail
            )
            profileNavGraph()
        }
    }
}
