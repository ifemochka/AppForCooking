// presentation/screens/MainScreen.kt
package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appforcooking.data.auth.AuthManager
import com.example.appforcooking.domain.models.ShownRecipes
import com.example.appforcooking.presentation.components.BottomNavigationBar
import com.example.appforcooking.presentation.data.FilterState

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var checkComplete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAuthenticated = authManager.isLoggedIn()
        checkComplete = true
    }

    if (!checkComplete) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (!isAuthenticated) {
        AuthScreen(
            navController = navController,
            onAuthSuccess = {
                isAuthenticated = true
            }
        )
    } else {
        AppContent(navController = navController)
    }
}

@Composable
fun AppContent(navController: NavHostController) {
    val currentRoute = currentRoute(navController)

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onItemClick = { item ->
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavigationHost(navController = navController)
        }
    }
}

@Composable
fun AppNavigationHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "products"
    ) {
        composable("products") {
            ProductListScreen()
        }

        composable("recipes") {
            RecipesScreen(navController = navController)
        }

        composable("shopping_list") {
            ShoppingListScreen(navController = navController)
        }

        composable("profile") {
            ProfileScreen(navController = navController)
        }

        composable("cooking_history") {
            CookingHistoryScreen(navController = navController)
        }

        composable(
            route = "recipe_detail/{recipeId}",
            arguments = listOf(
                androidx.navigation.navArgument("recipeId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0
            RecipeDetailScreen(recipeId = recipeId, navController = navController)
        }

        composable("recipe_filter") { backStackEntry ->
            RecipeFilterScreen(
                navController = navController
            )
        }

    }
}

@Composable
private fun currentRoute(navController: NavHostController): String {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route ?: "products"
}