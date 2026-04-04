package com.example.appforcooking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.dao.ShoppingListItemWithProduct
import com.example.appforcooking.data.repositories.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val repository: ShoppingListRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<ShoppingListItemWithProduct>>(emptyList())
    val items: StateFlow<List<ShoppingListItemWithProduct>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadItems(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getItems(userId).collectLatest { list ->
                _items.value = list
                _isLoading.value = false
            }
        }
    }

    fun togglePurchased(itemId: Long, currentStatus: Boolean, userId: Long) {
        viewModelScope.launch {
            repository.togglePurchased(itemId, currentStatus)
            loadItems(userId)
        }
    }

    fun clearPurchased(userId: Long) {
        viewModelScope.launch {
            repository.clearPurchased(userId)
            loadItems(userId)
        }
    }
}
