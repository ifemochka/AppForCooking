package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appforcooking.domain.models.ShownRecipes
import com.example.appforcooking.presentation.components.BottomNavigationBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()
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
            NavigationHost(navController = navController)
        }
    }
}

@Composable
fun NavigationHost(navController: NavHostController) {
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

        composable("profile") {
            ProfileScreen()
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
            val currentFilters = backStackEntry.savedStateHandle.get<ShownRecipes>("currentFilters")
                ?: ShownRecipes.all()

            RecipeFilterScreen(
                navController = navController,
                currentConfig = currentFilters
            )
        }

    }
}

@Composable
private fun currentRoute(navController: NavHostController): String {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route ?: "products"
}