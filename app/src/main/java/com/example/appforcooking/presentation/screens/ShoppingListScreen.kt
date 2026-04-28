package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.local.database.dao.ShoppingListItemWithProduct
import com.example.appforcooking.data.repositories.ProductRepository
import com.example.appforcooking.data.repositories.ShoppingListRepository
import com.example.appforcooking.presentation.viewmodels.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val userId = CookingDatabase.currentUserId

    val shoppingRepository = remember {
        val db = CookingDatabase.getDatabase(context)
        ShoppingListRepository(db.shoppingListDao())
    }

    val productRepository = remember {
        val db = CookingDatabase.getDatabase(context)
        ProductRepository(
            productDao = db.productDao(),
            pantryItemDao = db.pantryItemDao(),
            allergyDao = db.allergyDao()
        )
    }

    val viewModel: ShoppingListViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ShoppingListViewModel(shoppingRepository, productRepository) as T
            }
        }
    )

    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadItems(userId)
    }

    LaunchedEffect(message) {
        message?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val purchasedItems = items.filter { it.isPurchased }
    val unpurchasedItems = items.filter { !it.isPurchased }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список покупок") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (purchasedItems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.clearPurchased(userId) },
                    containerColor = Color(0xFF3949AB)
                ) {
                    Text("Добавить в холодильник")
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Список покупок пуст", style = MaterialTheme.typography.titleLarge)
                    Text("Добавьте продукты из рецептов", color = Color.Gray)
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
                if (unpurchasedItems.isNotEmpty()) {
                    item {
                        Text("Купить:", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                    items(unpurchasedItems) { item ->
                        ShoppingListItemCard(
                            item = item,
                            isPurchased = item.isPurchased,
                            onToggle = { viewModel.togglePurchased(item, item.isPurchased, userId) }
                        )
                    }
                }

                if (purchasedItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Куплено:", fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    items(purchasedItems) { item ->
                        ShoppingListItemCard(
                            item = item,
                            isPurchased = item.isPurchased,
                            onToggle = { viewModel.togglePurchased(item, item.isPurchased, userId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListItemCard(
    item: ShoppingListItemWithProduct,
    isPurchased: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPurchased) Color.LightGray else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onToggle) {
                    Icon(
                        if (isPurchased) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = if (isPurchased) "Отметить как некупленное" else "Отметить как купленное",
                        tint = if (isPurchased) Color(0xFF4CAF50) else Color.Gray
                    )
                }
                Column {
                    Text(
                        item.productName,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (isPurchased) TextDecoration.LineThrough else null,
                        color = if (isPurchased) Color.Gray else Color.Black
                    )
                    Text(
                        item.category,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}