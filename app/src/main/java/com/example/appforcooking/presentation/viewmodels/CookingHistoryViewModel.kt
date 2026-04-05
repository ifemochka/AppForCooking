package com.example.appforcooking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.dao.CookingHistoryWithRecipe
import com.example.appforcooking.domain.usecases.GetUserCookingHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CookingHistoryViewModel(
    private val getUserCookingHistoryUseCase: GetUserCookingHistoryUseCase
) : ViewModel() {

    private val _history = MutableStateFlow<List<CookingHistoryWithRecipe>>(emptyList())
    val history: StateFlow<List<CookingHistoryWithRecipe>> = _history.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadHistory(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            getUserCookingHistoryUseCase(userId).collectLatest { list ->
                _history.value = list
                _isLoading.value = false
            }
        }
    }
}