package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.repositories.RecipeRepository
import com.example.appforcooking.domain.usecases.GetRecipeByIdUseCase
import com.example.appforcooking.domain.usecases.GetRecipeIngredientsUseCase
import com.example.appforcooking.presentation.components.RecipeDetailContent
import com.example.appforcooking.presentation.viewmodels.RecipeDetailViewModel
import com.example.appforcooking.ui.theme.Fonts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavHostController
) {
    val context = LocalContext.current

    val viewModel: RecipeDetailViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val database = CookingDatabase.getDatabase(context)
                val recipeDao = database.recipeDao()
                val pantryItemDao = database.pantryItemDao()
                val productDao = database.productDao()
                val repository = RecipeRepository(recipeDao, pantryItemDao, productDao)

                return RecipeDetailViewModel(
                    recipeId = recipeId,
                    context = context,
                    getRecipeByIdUseCase = GetRecipeByIdUseCase(repository),
                    getRecipeIngredientsUseCase = GetRecipeIngredientsUseCase(repository)
                ) as T
            }
        }
    )

    val recipe by viewModel.recipe.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val availableCount by viewModel.availableCount.collectAsState()
    val isAddingToShoppingList by viewModel.isAddingToShoppingList.collectAsState()
    val shoppingListMessage by viewModel.shoppingListMessage.collectAsState()

    LaunchedEffect(shoppingListMessage) {
        shoppingListMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Детали рецепта",
                        fontFamily = Fonts.font,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3949AB),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF3949AB)
                )
            }
        } else if (recipe == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Рецепт не найден",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            RecipeDetailContent(
                recipe = recipe!!,
                ingredients = ingredients,
                availableCount = availableCount,
                isAddingToShoppingList = isAddingToShoppingList,
                onAddToShoppingList = { viewModel.addMissingIngredientsToShoppingList() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}