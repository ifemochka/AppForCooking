package com.example.appforcooking.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

object NavigationItems {
    val items = listOf(
        BottomNavItem(
            name = "Продукты",
            route = "products",
            icon = Icons.Default.Fastfood
        ),
        BottomNavItem(
            name = "Рецепты",
            route = "recipes",
            icon = Icons.Default.RestaurantMenu
        ),
        BottomNavItem(
            "Список",
            "shopping_list",
            Icons.Default.ShoppingCart
        ),
        BottomNavItem(
            name = "Профиль",
            route = "profile",
            icon = Icons.Default.Person
        )
    )
}