package com.example.appforcooking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.data.auth.AuthManager
import com.example.appforcooking.data.server.dto.LoginRequest
import com.example.appforcooking.data.server.dto.RegisterRequest
import com.example.appforcooking.data.server.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))

                if (response.success && response.token != null) {
                    authManager.saveAuthData(
                        token = response.token,
                        userId = response.userId ?: 0,
                        email = response.email ?: "",
                        firstName = response.firstName ?: "",
                        lastName = response.lastName ?: ""
                    )
                    _authSuccess.value = true
                } else {
                    _error.value = response.error ?: "Ошибка входа"
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(email, password, firstName, lastName)
                )

                if (response.success && response.token != null) {
                    authManager.saveAuthData(
                        token = response.token,
                        userId = response.userId ?: 0,
                        email = response.email ?: "",
                        firstName = response.firstName ?: "",
                        lastName = response.lastName ?: ""
                    )
                    _authSuccess.value = true
                } else {
                    _error.value = response.error ?: "Ошибка регистрации"
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}