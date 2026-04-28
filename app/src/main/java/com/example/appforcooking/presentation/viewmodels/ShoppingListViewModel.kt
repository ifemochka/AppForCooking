package com.example.appforcooking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.dao.ShoppingListItemWithProduct
import com.example.appforcooking.data.repositories.ProductRepository
import com.example.appforcooking.data.repositories.ShoppingListRepository
import com.example.appforcooking.data.repositories.SyncChangesRepository
import com.example.appforcooking.data.server.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val shoppingListRepository: ShoppingListRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val syncRepository = SyncChangesRepository(
        RetrofitClient.apiService
    )

    private val _items = MutableStateFlow<List<ShoppingListItemWithProduct>>(emptyList())
    val items: StateFlow<List<ShoppingListItemWithProduct>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadItems(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            shoppingListRepository.getItems(userId).collectLatest { list ->
                _items.value = list
                _isLoading.value = false
            }
        }
    }

    fun togglePurchased(item: ShoppingListItemWithProduct, currentStatus: Boolean, userId: Long) {
        viewModelScope.launch {
            shoppingListRepository.togglePurchased(item.id, currentStatus)
            val serverSuccess = syncRepository.updateShoppingItemStatusOnServer(
                userId,
                item.productId,
                !currentStatus
            )
            loadItems(userId)
        }
    }


    fun clearPurchased(userId: Long) {
        viewModelScope.launch {
            try {
                // Получаем купленные продукты
                val purchasedItems = _items.value.filter { it.isPurchased }

                if (purchasedItems.isEmpty()) {
                    _message.value = "Нет купленных продуктов для добавления"
                    kotlinx.coroutines.delay(2000)
                    _message.value = null
                    return@launch
                }

                var addedCount = 0

                for (item in purchasedItems) {
                    try {
                        productRepository.addProductToUser(userId, item.productId)
                        val serverSuccess = syncRepository.addProductToPantryOnServer(userId, item.productId)
                        addedCount++
                    } catch (e: Exception) {
                    }
                }

                for (item in purchasedItems) {
                    try {
                        val serverSuccess = syncRepository.removePurchasedProducts(userId, item.productId)
                    } catch (e: Exception) {
                    }
                }
                shoppingListRepository.clearPurchased(userId)

                loadItems(userId)

                _message.value = "Добавлено $addedCount продуктов в холодильник"

                kotlinx.coroutines.delay(3000)
                _message.value = null

            } catch (e: Exception) {
                _message.value = "Ошибка: ${e.message}"
                kotlinx.coroutines.delay(3000)
                _message.value = null
            }
        }
    }
}