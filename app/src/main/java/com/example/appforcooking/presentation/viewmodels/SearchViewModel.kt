package com.example.appforcooking.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.domain.models.Product
import com.example.appforcooking.domain.usecases.AddAllergyToUserUseCase
import com.example.appforcooking.domain.usecases.AddProductToPantryUseCase
import com.example.appforcooking.domain.usecases.SearchProductsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val addProductToPantryUseCase: AddProductToPantryUseCase,
    private val addAllergyToUserUseCase: AddAllergyToUserUseCase

) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    var searchResults by mutableStateOf<List<Product>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        searchQuery = query

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(300)
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        if (query.length < 2) {
            searchResults = emptyList()
            return
        }

        isLoading = true
        error = null

        try {
            val results = searchProductsUseCase(query)
            searchResults = results
            Log.d("SearchViewModel", "Найдено продуктов: ${results.size}")
        } catch (e: Exception) {
            error = "Ошибка поиска: ${e.message}"
            Log.e("SearchViewModel", "Ошибка поиска", e)
        } finally {
            isLoading = false
        }
    }

    fun addProductToPantry(product: Product) {
        viewModelScope.launch {
            try {
                val userId = CookingDatabase.currentUserId
                addProductToPantryUseCase(userId, product.productId.toLong())
                successMessage = "Продукт '${product.name}' добавлен в холодильник"
                searchQuery = ""
                searchResults = emptyList()
                Log.d("SearchViewModel", "Продукт ${product.name} добавлен пользователю $userId")
            } catch (e: Exception) {
                error = "Ошибка добавления: ${e.message}"
                Log.e("SearchViewModel", "Ошибка добавления", e)
            }
        }
    }

    fun addAllergyToPantry(product: Product) {
        viewModelScope.launch {
            try {
                val userId = CookingDatabase.currentUserId
                addAllergyToUserUseCase(userId, product.productId.toLong())
                successMessage = "Аллергия на продукт ${product.name} добавлен"
                searchQuery = ""
                searchResults = emptyList()
                Log.d("SearchViewModel", "Аллергия на продукт ${product.name} добавлена пользователю $userId")
            } catch (e: Exception) {
                error = "Ошибка добавления: ${e.message}"
                Log.e("SearchViewModel", "Ошибка добавления", e)
            }
        }
    }

    fun clearMessages() {
        error = null
        successMessage = null
    }
}