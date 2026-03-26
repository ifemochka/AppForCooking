package com.example.appforcooking.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            name = "Поиск",
            route = "recipes",
            icon = Icons.Default.Search
        ),
        BottomNavItem(
            name = "Профиль",
            route = "profile",
            icon = Icons.Default.Person
        )
    )
}