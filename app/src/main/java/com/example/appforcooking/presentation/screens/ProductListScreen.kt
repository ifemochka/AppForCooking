package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appforcooking.R
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.repositories.ProductRepository
import com.example.appforcooking.domain.models.CategoryWithProducts
import com.example.appforcooking.domain.usecases.AddAllergyToUserUseCase
import com.example.appforcooking.domain.usecases.AddProductToPantryUseCase
import com.example.appforcooking.domain.usecases.GetUserProductsUseCase
import com.example.appforcooking.domain.usecases.RemoveProductFromPantryUseCase
import com.example.appforcooking.domain.usecases.SearchProductsUseCase
import com.example.appforcooking.presentation.components.CategoryCard
import com.example.appforcooking.presentation.components.ProductSearchItem
import com.example.appforcooking.presentation.viewmodels.ProductViewModel
import com.example.appforcooking.presentation.viewmodels.SearchViewModel
import com.example.appforcooking.ui.theme.Fonts.font
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen() {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Состояние для режима поиска
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Поиска
    val searchViewModel: SearchViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val database = CookingDatabase.getDatabase(context)
                val repository = ProductRepository(
                    database.productDao(),
                    database.pantryItemDao(),
                    database.allergyDao()
                )
                return SearchViewModel(
                    searchProductsUseCase = SearchProductsUseCase(repository),
                    addProductToPantryUseCase = AddProductToPantryUseCase(repository),
                    addAllergyToUserUseCase = AddAllergyToUserUseCase(repository)
                ) as T
            }
        }
    )

    // Продукты
    val productViewModel: ProductViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val database = CookingDatabase.getDatabase(context)
                val repository = ProductRepository(
                    database.productDao(),
                    database.pantryItemDao(),
                    database.allergyDao()
                )
                return ProductViewModel(
                    getUserProductsUseCase = GetUserProductsUseCase(repository),
                    removeProductFromPantryUseCase = RemoveProductFromPantryUseCase(repository)
                ) as T
            }
        }
    )

    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val error by productViewModel.error.collectAsState()
    val expandedCategories by productViewModel.expandedCategories.collectAsState()

    val searchResults by remember { derivedStateOf { searchViewModel.searchResults } }
    val isSearchLoading by remember { derivedStateOf { searchViewModel.isLoading } }
    val searchError by remember { derivedStateOf { searchViewModel.error } }
    val successMessage by remember { derivedStateOf { searchViewModel.successMessage } }

    val categoriesWithProducts = remember(products, expandedCategories) {
        products.groupBy { it.category }
            .map { (category, productList) ->
                CategoryWithProducts(
                    category = category,
                    products = productList,
                    isExpanded = category in expandedCategories
                )
            }
            .sortedBy { it.category }
    }

    // Очищаем сообщения через 3 секунды
    LaunchedEffect(successMessage, searchError) {
        if (successMessage != null || searchError != null) {
            delay(3000)
            searchViewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Фон кулинарии",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Ваши продукты",
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF703D00),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp,
                    fontFamily = font
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                maxLines = 1
            )

            // Строка поиска
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    searchViewModel.onSearchQueryChanged(it)
                    isSearching = it.isNotEmpty()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск продуктов...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchViewModel.onSearchQueryChanged("")
                            isSearching = false
                            keyboardController?.hide()
                        }) {
                            Text("×", fontSize = 24.sp)
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (searchError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = searchError!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isSearching) {
                // Показываем результаты поиска
                if (isSearchLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (searchQuery.length >= 2 && searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ничего не найдено",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(searchResults) { product ->
                            ProductSearchItem(
                                product = product,
                                onAddClick = {
                                    searchViewModel.addProductToPantry(product)
                                    productViewModel.loadUserProducts()
                                    keyboardController?.hide()
                                    searchQuery = ""
                                    isSearching = false
                                }
                            )
                        }
                    }
                }
            } else {
                // Показываем продукты пользователя
                error?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (categoriesWithProducts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Нет данных о продуктах",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(categoriesWithProducts) { categoryWithProducts ->
                                CategoryCard(
                                    categoryWithProducts = categoryWithProducts,
                                    onExpandedChange = {
                                        productViewModel.toggleCategory(categoryWithProducts.category)
                                    },
                                    onProductDelete = { product ->
                                        productViewModel.removeProduct(product)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

