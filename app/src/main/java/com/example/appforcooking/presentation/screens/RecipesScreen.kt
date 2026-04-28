package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appforcooking.R
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.repositories.RecipeRepository
import com.example.appforcooking.domain.models.ShownRecipes
import com.example.appforcooking.domain.usecases.GetAllRecipesUseCase
import com.example.appforcooking.domain.usecases.GetAllRecipesWithPercentageUseCase
import com.example.appforcooking.domain.usecases.GetAvailableRecipesUseCase
import com.example.appforcooking.presentation.components.RecipeItem
import com.example.appforcooking.presentation.data.FilterState
import com.example.appforcooking.presentation.viewmodels.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(navController: NavHostController) {
    val context = LocalContext.current

    val viewModel: RecipeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val database = CookingDatabase.getDatabase(context)
                val recipeDao = database.recipeDao()
                val pantryItemDao = database.pantryItemDao()
                val productDao = database.productDao()
                val allergyDao = database.allergyDao()

                val repository = RecipeRepository(
                    recipeDao = recipeDao,
                    pantryItemDao = pantryItemDao,
                    productDao = productDao
                )

                return RecipeViewModel(
                    getAllRecipesUseCase = GetAllRecipesUseCase(repository),
                    getAllRecipesWithPercentageUseCase = GetAllRecipesWithPercentageUseCase(
                        repository
                    ),
                    getAvailableRecipesUseCase = GetAvailableRecipesUseCase(repository),
                    recipeDao = recipeDao,
                    allergyDao = allergyDao
                ) as T
            }
        }
    )

    val shownRecipes by viewModel.shownRecipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val shownRecipesConfig by viewModel.shownRecipesConfig.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.updateShownRecipesConfig(FilterState.currentFilters)
    }

    LaunchedEffect(shownRecipesConfig) {
        FilterState.currentFilters = shownRecipesConfig
    }

    val isFilterActive = listOf(
        !shownRecipesConfig.showEasy,
        !shownRecipesConfig.showMid,
        !shownRecipesConfig.showHard,
        !shownRecipesConfig.hideAllergyRecipes
    ).any { it }

    val filterResult = navController.currentBackStackEntry?.savedStateHandle?.getLiveData<ShownRecipes>("filterResult")
    LaunchedEffect(filterResult) {
        filterResult?.observeForever { newConfig ->
            viewModel.updateShownRecipesConfig(newConfig)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.yasuo),
            contentDescription = "Фон кулинарии",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Spacer(modifier = Modifier.height(5.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Рецепты",
                    style = TextStyle(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3949AB),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.align(Alignment.Center),
                    maxLines = 1
                )

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.CenterEnd),
                    contentAlignment = Alignment.Center
                ) {
                    BadgedBox(
                        badge = {
                            if (isFilterActive) {
                                Badge(
                                    containerColor = Color(0xFF3949AB),
                                    contentColor = Color.White,
                                    modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                                ) {
                                    val count = listOf(
                                        !shownRecipesConfig.showEasy,
                                        !shownRecipesConfig.showMid,
                                        !shownRecipesConfig.showHard,
                                        !shownRecipesConfig.hideAllergyRecipes
                                    ).count { it }
                                    Text("$count", fontSize = 11.sp)
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("currentFilters", shownRecipesConfig)
                                navController.navigate("recipe_filter")
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Фильтр",
                                tint = if (isFilterActive) Color(0xFF3949AB) else Color(0xFF3949AB),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    label = { Text("Все рецепты") },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    label = { Text("Могу приготовить") },
                    modifier = Modifier.weight(1f)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (shownRecipes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedTab == 0)
                                "Рецепты не найдены"
                            else
                                "Нет рецептов, которые можно приготовить из ваших продуктов",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        if (isFilterActive) {
                            TextButton(
                                onClick = {
                                    viewModel.updateShownRecipesConfig(ShownRecipes.all())
                                }
                            ) {
                                Text("Сбросить фильтр")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(shownRecipes) { recipe ->
                        RecipeItem(
                            recipe = recipe,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                navController.navigate("recipe_detail/${recipe.recipeId}")
                            }
                        )
                    }
                }
            }
        }
    }
}