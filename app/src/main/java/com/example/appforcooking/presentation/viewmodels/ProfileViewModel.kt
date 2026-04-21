package com.example.appforcooking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.auth.AuthManager
import com.example.appforcooking.data.repositories.ServerRepository
import com.example.appforcooking.domain.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authManager: AuthManager,
    private val serverRepository: ServerRepository
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
                val token = authManager.getToken()
                if (token.isNotEmpty()) {
                    val profile = serverRepository.getUserProfile(token)
                    if (profile != null) {
                        _userProfile.value = profile
                    } else {
                        _userProfile.value = UserProfile(
                            userId = authManager.getUserId(),
                            email = authManager.getUserEmail(),
                            firstName = authManager.getFirstName(),
                            lastName = authManager.getLastName(),
                            birthDate = null,
                            avatarUrl = null
                        )
                    }
                } else {
                    _userProfile.value = UserProfile(
                        userId = authManager.getUserId(),
                        email = authManager.getUserEmail(),
                        firstName = authManager.getFirstName(),
                        lastName = authManager.getLastName(),
                        birthDate = null,
                        avatarUrl = null
                    )
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки профиля: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(firstName: String?, lastName: String?) {
        // TODO: Обновление профиля на сервере
        _successMessage.value = "Профиль успешно обновлен"
        loadUserProfile()
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}