package com.example.appforcooking.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.domain.models.Product
import com.example.appforcooking.domain.usecases.GetUserProductsUseCase
import com.example.appforcooking.domain.usecases.RemoveProductFromPantryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductViewModel(
    private val getUserProductsUseCase: GetUserProductsUseCase,
    private val removeProductFromPantryUseCase: RemoveProductFromPantryUseCase
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _expandedCategories = MutableStateFlow<Set<String>>(emptySet())
    val expandedCategories: StateFlow<Set<String>> = _expandedCategories.asStateFlow()

    init {
        loadUserProducts()
    }

    fun loadUserProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = CookingDatabase.currentUserId
                Log.d("ProductViewModel", "Загрузка продуктов для пользователя ID: $userId")

                getUserProductsUseCase(userId).collectLatest { productsList ->
                    Log.d("ProductViewModel", "Получено продуктов: ${productsList.size}")
                    _products.value = productsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                Log.e("ProductViewModel", "Ошибка загрузки базы: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun removeProduct(product: Product) {
        viewModelScope.launch {
            try {
                val userId = CookingDatabase.currentUserId
                removeProductFromPantryUseCase(userId, product.productId)

                _successMessage.value = "Продукт '${product.name}' удален из холодильника"

                // Обновляем список продуктов
                loadUserProducts()

                Log.d("ProductViewModel", "Продукт ${product.name} удален у пользователя $userId")
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
                Log.e("ProductViewModel", "Ошибка удаления", e)
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }

    fun toggleCategory(category: String) {
        _expandedCategories.value = if (category in _expandedCategories.value) {
            _expandedCategories.value - category
        } else {
            _expandedCategories.value + category
        }
    }
}