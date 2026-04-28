package com.example.appforcooking.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.repositories.SyncChangesRepository
import com.example.appforcooking.data.server.RetrofitClient
import com.example.appforcooking.domain.models.Product
import com.example.appforcooking.domain.usecases.GetUserAllergiesUseCase
import com.example.appforcooking.domain.usecases.RemoveAllergyFromUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AllergyViewModel (
    private val getUserAllergiesUseCase: GetUserAllergiesUseCase,
    private val removeAllergyFromUserUseCase: RemoveAllergyFromUserUseCase
) : ViewModel() {

    private val syncRepository = SyncChangesRepository(
        RetrofitClient.apiService
    )

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadUserAllergies()
    }

    fun loadUserAllergies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = CookingDatabase.currentUserId
                Log.d("AllergyViewModel", "Загрузка аллергий для пользователя ID: $userId")

                getUserAllergiesUseCase(userId).collectLatest { productsList ->
                    Log.d("AllergyViewModel", "Получено аллергий: ${productsList.size}")
                    _products.value = productsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
                Log.e("AllergyViewModel", "Ошибка загрузки базы: ${e.message}")
                _isLoading.value = false
            }
        }
    }

    fun removeAllergy(product: Product) {
        viewModelScope.launch {
            try {
                val userId = CookingDatabase.currentUserId
                removeAllergyFromUserUseCase(userId, product.productId)

                val serverSuccess = syncRepository.removeAllergyOnServer(userId, product.productId)

                if (serverSuccess) {
                    _successMessage.value = "Аллергия на '${product.name}' удалена (синхронизировано)"
                } else {
                    _successMessage.value = "Аллергия на '${product.name}' удалена локально, но не синхронизирована"
                }

                loadUserAllergies()

                Log.d("AllergyViewModel", "Аллергия на ${product.name} удалена, синхронизация: $serverSuccess")
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
                Log.e("AllergyViewModel", "Ошибка удаления", e)
            }
        }
    }


    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}