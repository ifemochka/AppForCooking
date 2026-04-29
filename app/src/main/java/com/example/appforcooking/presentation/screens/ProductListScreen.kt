package com.example.appforcooking.presentation.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appforcooking.R
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.repositories.ProductRepository
import com.example.appforcooking.domain.models.CategoryWithProducts
import com.example.appforcooking.domain.models.Product
import com.example.appforcooking.domain.models.toDomain
import com.example.appforcooking.domain.usecases.AddAllergyToUserUseCase
import com.example.appforcooking.domain.usecases.AddProductToPantryUseCase
import com.example.appforcooking.domain.usecases.GetUserProductsUseCase
import com.example.appforcooking.domain.usecases.RemoveProductFromPantryUseCase
import com.example.appforcooking.domain.usecases.SearchProductsUseCase
import com.example.appforcooking.domain.utils.VoiceTextProcessor
import com.example.appforcooking.presentation.components.AmbiguousProductDialog
import com.example.appforcooking.presentation.components.CategoryCard
import com.example.appforcooking.presentation.components.ProductSearchItem
import com.example.appforcooking.presentation.components.VoiceRecognitionResultDialog
import com.example.appforcooking.presentation.viewmodels.ProductViewModel
import com.example.appforcooking.presentation.viewmodels.SearchViewModel
import com.example.appforcooking.ui.theme.Fonts.font
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen() {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Состояние для режима поиска
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isVoiceProcessing by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var uniqueProducts by remember { mutableStateOf<Map<String, Product>>(emptyMap()) }
    var ambiguousProducts by remember { mutableStateOf<Map<String, List<Product>>>(emptyMap()) }
    var notFoundProducts by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentAmbiguous by remember { mutableStateOf<Pair<String, List<Product>>?>(null) }

    val allProducts = remember { mutableStateOf<List<Product>>(emptyList()) }
    val voiceProcessor = remember {
        VoiceTextProcessor(
            getAllProducts = { allProducts.value }
        )
    }

    // Поиск
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

    LaunchedEffect(Unit) {
        val database = CookingDatabase.getDatabase(context)
        val productsEntities = database.productDao().getAllProducts().firstOrNull() ?: emptyList()
        allProducts.value = productsEntities.map { it.toDomain() }
    }

    // Очищаем сообщения через 3 секунды
    LaunchedEffect(successMessage, searchError) {
        if (successMessage != null || searchError != null) {
            delay(3000)
            searchViewModel.clearMessages()
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                recognizedText = spokenText
                isVoiceProcessing = true

                (searchViewModel.viewModelScope).launch {
                    val processingResult = voiceProcessor.processVoiceText(spokenText)
                    uniqueProducts = processingResult.uniqueProducts
                    ambiguousProducts = processingResult.ambiguousProducts
                    notFoundProducts = processingResult.notFound

                    if (ambiguousProducts.isNotEmpty()) {
                        val first = ambiguousProducts.entries.first()
                        currentAmbiguous = first.key to first.value
                    }
                }
            } else {
                Toast.makeText(context, "Не удалось распознать речь", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Голосовой ввод отменен", Toast.LENGTH_SHORT).show()
        }
    }

    fun startVoiceRecognition() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Нет разрешения на использование микрофона", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите на русском языке")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)

        }

        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
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

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    searchViewModel.onSearchQueryChanged(it)
                    isSearching = it.isNotEmpty()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Добавить продукт") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { startVoiceRecognition() }) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Голосовой ввод",
                                tint = Color(0xFF3949AB)
                            )
                        }
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

    if (isVoiceProcessing && currentAmbiguous == null) {
        VoiceRecognitionResultDialog(
            recognizedText = recognizedText,
            uniqueProducts = uniqueProducts,
            ambiguousProducts = ambiguousProducts,
            notFound = notFoundProducts,
            onAddProduct = { product ->
                searchViewModel.addProductToPantry(product)
                productViewModel.loadUserProducts()
            },
            onAddAllUnique = {
                uniqueProducts.values.forEach { product ->
                    searchViewModel.addProductToPantry(product)
                }
                productViewModel.loadUserProducts()
                isVoiceProcessing = false
            },
            onResolveAmbiguous = { name, options ->
                currentAmbiguous = name to options
            },
            onDismiss = {
                isVoiceProcessing = false
            }
        )
    }

    if (currentAmbiguous != null) {
        AmbiguousProductDialog(
            productName = currentAmbiguous!!.first,
            options = currentAmbiguous!!.second,
            onSelect = { product ->
                searchViewModel.addProductToPantry(product)
                productViewModel.loadUserProducts()

                val newAmbiguous = ambiguousProducts.toMutableMap()
                newAmbiguous.remove(currentAmbiguous!!.first)
                ambiguousProducts = newAmbiguous
                currentAmbiguous = null

                if (ambiguousProducts.isNotEmpty()) {
                    val next = ambiguousProducts.entries.first()
                    currentAmbiguous = next.key to next.value
                } else if (uniqueProducts.isEmpty()) {
                    isVoiceProcessing = false
                }
            },
            onSkip = {
                val newAmbiguous = ambiguousProducts.toMutableMap()
                newAmbiguous.remove(currentAmbiguous!!.first)
                ambiguousProducts = newAmbiguous
                currentAmbiguous = null

                if (ambiguousProducts.isNotEmpty()) {
                    val next = ambiguousProducts.entries.first()
                    currentAmbiguous = next.key to next.value
                } else if (uniqueProducts.isEmpty()) {
                    isVoiceProcessing = false
                }
            },
            onDismiss = {
                currentAmbiguous = null
                isVoiceProcessing = false
            }
        )
    }
}