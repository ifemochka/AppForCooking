package com.example.appforcooking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.domain.models.UserProfile
import com.example.appforcooking.domain.usecases.GetUserProfileUseCase
import com.example.appforcooking.domain.usecases.UpdateUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = CookingDatabase.currentUserId
                val profile = getUserProfileUseCase(userId)
                _userProfile.value = profile
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки профиля: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(firstName: String?, lastName: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = CookingDatabase.currentUserId
                val success = updateUserProfileUseCase(userId, firstName, lastName)

                if (success) {
                    _successMessage.value = "Профиль успешно обновлен"
                    // Перезагружаем профиль
                    loadUserProfile()
                } else {
                    _error.value = "Не удалось обновить профиль"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка обновления: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}