package com.example.appforcooking.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appforcooking.CookingApplication
import com.example.appforcooking.data.auth.AuthManager
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.repositories.SyncRepository
import com.example.appforcooking.data.server.RetrofitClient
import com.example.appforcooking.data.server.dto.LoginRequest
import com.example.appforcooking.data.server.dto.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _navigationEvent = MutableStateFlow(false)
    val navigationEvent: StateFlow<Boolean> = _navigationEvent.asStateFlow()

    private val TAG = "AuthViewModel"

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "Попытка входа для email: $email")

            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                Log.d(TAG, "Ответ от сервера: success=${response.success}, userId=${response.userId}")

                if (response.success && response.token != null) {
                    val userId = response.userId ?: 0
                    Log.d(TAG, "Логин успешен userId=$userId")

                    authManager.saveLoginData(
                        email = response.email ?: "",
                        userId = userId,
                        firstName = response.firstName ?: "",
                        lastName = response.lastName ?: ""
                    )

                    CookingDatabase.currentUserId = userId
                    Log.d(TAG, "Установлен currentUserId = ${CookingDatabase.currentUserId}")

                    _isSyncing.value = true

                    val syncRepository = SyncRepository(
                        CookingApplication.appContext,
                        RetrofitClient.apiService
                    )

                    val syncSuccess = syncRepository.syncUserData(userId)
                    Log.d(TAG, "Синхронизация завершена: success=$syncSuccess")

                    _isSyncing.value = false

                    if (syncSuccess) {
                        Log.d(TAG, "Авторизация и синхронизация успешны")
                        _isAuthenticated.value = true
                        _navigationEvent.value = true
                    } else {
                        Log.e(TAG, "Ошибка синхронизации данных")
                        _error.value = "Ошибка синхронизации данных"
                    }
                } else {
                    Log.e(TAG, "Ошибка логина: ${response.error}")
                    _error.value = response.error ?: "Ошибка входа"
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP ошибка: ${e.code()} - ${e.message()}")
                when (e.code()) {
                    401 -> {
                        _error.value = "Неверный email или пароль. Проверьте введённые данные."
                    }
                    404 -> {
                        _error.value = "Сервер не найден. Проверьте подключение."
                    }
                    500 -> {
                        _error.value = "Ошибка на сервере. Попробуйте позже."
                    }
                    else -> {
                        _error.value = "Ошибка сервера: ${e.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при логине: ${e.message}", e)

                val errorMessage = when {
                    e.message?.contains("timeout") == true -> "Превышено время ожидания. Проверьте соединение."
                    e.message?.contains("Failed to connect") == true -> "Не удалось подключиться к серверу. Проверьте интернет."
                    e.message?.contains("UnknownHost") == true -> "Сервер недоступен. Проверьте адрес сервера."
                    else -> "Ошибка подключения к серверу: ${e.message}"
                }
                _error.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d(TAG, "Попытка регистрации для email: $email, FirstName: $firstName, LastName: $lastName")

            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(email, password, firstName, lastName)
                )
                Log.d(TAG, "Ответ от сервера: success=${response.success}, userId=${response.userId}")

                if (response.success && response.token != null) {
                    val userId = response.userId ?: 0
                    Log.d(TAG, "Регистрация успешна userId=$userId")

                    authManager.saveLoginData(
                        email = response.email ?: "",
                        userId = userId,
                        firstName = response.firstName ?: "",
                        lastName = response.lastName ?: ""
                    )

                    CookingDatabase.currentUserId = userId
                    Log.d(TAG, "Установлен currentUserId = ${CookingDatabase.currentUserId}")

                    _isSyncing.value = true

                    val syncRepository = SyncRepository(
                        CookingApplication.appContext,
                        RetrofitClient.apiService
                    )

                    val syncSuccess = syncRepository.syncUserData(userId)
                    Log.d(TAG, "Синхронизация после регистрации: success=$syncSuccess")

                    _isSyncing.value = false

                    if (syncSuccess) {
                        Log.d(TAG, "Регистрация и синхронизация успешны, currentUserId=$userId")
                        _isAuthenticated.value = true
                        _navigationEvent.value = true
                    } else {
                        Log.e(TAG, "Ошибка синхронизации после регистрации")
                        _error.value = "Ошибка синхронизации данных"
                    }
                } else {
                    Log.e(TAG, "Ошибка регистрации: ${response.error}")
                    _error.value = response.error ?: "Ошибка регистрации"
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP ошибка при регистрации: ${e.code()} - ${e.message()}")
                when (e.code()) {
                    400 -> {
                        _error.value = "Пользователь с таким email уже существует или данные некорректны"
                    }
                    409 -> {
                        _error.value = "Пользователь с таким email уже зарегистрирован"
                    }
                    401 -> {
                        _error.value = "Ошибка авторизации при регистрации"
                    }
                    500 -> {
                        _error.value = "Ошибка на сервере. Попробуйте позже."
                    }
                    else -> {
                        _error.value = "Ошибка сервера: ${e.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Исключение при регистрации: ${e.message}", e)

                val errorMessage = when {
                    e.message?.contains("timeout") == true -> "Превышено время ожидания. Проверьте соединение."
                    e.message?.contains("Failed to connect") == true -> "Не удалось подключиться к серверу. Проверьте интернет."
                    e.message?.contains("UnknownHost") == true -> "Сервер недоступен. Проверьте адрес сервера."
                    else -> "Ошибка подключения к серверу: ${e.message}"
                }
                _error.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        Log.d(TAG, "Сброс состояния AuthViewModel")
        _isAuthenticated.value = false
        _navigationEvent.value = false
        _error.value = null
        _isLoading.value = false
        _isSyncing.value = false
    }

    fun consumeNavigationEvent() {
        _navigationEvent.value = false
    }

    fun clearError() {
        _error.value = null
    }
}