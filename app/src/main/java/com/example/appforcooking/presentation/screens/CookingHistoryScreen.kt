package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.local.database.dao.CookingHistoryWithRecipe
import com.example.appforcooking.data.repositories.CookingHistoryRepository
import com.example.appforcooking.domain.usecases.GetUserCookingHistoryUseCase
import com.example.appforcooking.presentation.viewmodels.CookingHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingHistoryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val userId = CookingDatabase.currentUserId

    val repository = remember {
        val db = CookingDatabase.getDatabase(context)
        CookingHistoryRepository(db.cookingHistoryDao())
    }

    val viewModel: CookingHistoryViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CookingHistoryViewModel(
                    GetUserCookingHistoryUseCase(repository)
                ) as T
            }
        }
    )

    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHistory(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История приготовлений") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("История пуста", style = MaterialTheme.typography.titleLarge)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { item ->
                    HistoryItemCard(
                        item = item,
                        onClick = {
                            navController.navigate("recipe_detail/${item.recipeId}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: CookingHistoryWithRecipe,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(item.cookedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⏱ ${item.cookingTimeMinutes} мин • ${item.difficulty}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = dateString,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}